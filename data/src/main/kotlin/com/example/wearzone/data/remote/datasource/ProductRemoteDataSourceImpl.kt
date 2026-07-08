package com.example.wearzone.data.remote.datasource

import android.os.SystemClock
import android.util.Log
import com.example.wearzone.data.remote.api.ProductApiService
import com.example.wearzone.data.remote.dto.BrandDto
import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.ProductDto
import com.example.wearzone.data.remote.dto.ProductLocalizationFields
import com.example.wearzone.data.remote.dto.ProductLocalizationGraphQlVariables
import com.example.wearzone.data.remote.dto.ShopifyGraphQlRequest
import com.example.wearzone.data.remote.dto.ShopifyProduct
import com.example.wearzone.data.remote.dto.ShopifyProductDetail
import com.example.wearzone.data.remote.dto.dashboardBrands
import com.example.wearzone.data.remote.dto.dashboardCategories
import com.example.wearzone.data.remote.dto.toGraphQlProductLocalizationFields
import com.example.wearzone.data.remote.dto.vendorBrands
import com.example.wearzone.data.remote.dto.withVendorImages
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class ProductRemoteDataSourceImpl @Inject constructor(
    private val apiService: ProductApiService,
) : IProductRemoteDataSource {

    private companion object {
        const val SHOPIFY_MAX_PAGE_SIZE = 250
        const val LOCALIZATION_NAMESPACE = "wearzone"
        const val LOCALIZATION_FETCH_PARALLELISM = 4
        const val LOCALIZATION_BATCH_SIZE = 50
        const val TAG = "ProductApiTiming"

        const val PRODUCT_LOCALIZATION_QUERY = """
            query ProductLocalization(${'$'}ids: [ID!]!, ${'$'}namespace: String!) {
              nodes(ids: ${'$'}ids) {
                ... on Product {
                  legacyResourceId
                  metafields(first: 20, namespace: ${'$'}namespace) {
                    nodes {
                      namespace
                      key
                      value
                    }
                  }
                }
              }
            }
        """
    }

    private val localizationCache = ConcurrentHashMap<Long, ProductLocalizationFields>()
    private val productListCache = ConcurrentHashMap<String, List<ProductDto>>()
    private val productDetailCache = ConcurrentHashMap<Long, ShopifyProductDetail>()
    private val brandCache = ConcurrentHashMap<String, List<BrandDto>>()
    private val categoryCache = ConcurrentHashMap<String, List<CategoryDto>>()
    private val localizationSemaphore = Semaphore(LOCALIZATION_FETCH_PARALLELISM)

    override suspend fun getCategories(): List<CategoryDto> = logTimed("categories total") {
        categoryCache["all"]?.let { cached ->
            Log.d(TAG, "categories cache hit size=${cached.size}")
            return@logTimed cached
        }
        val categories = logTimed("GET custom_collections categories") {
            apiService.getCustomCollections().custom_collections.dashboardCategories()
        }
        categoryCache["all"] = categories
        categories
    }

    override suspend fun getBrands(): List<BrandDto> = logTimed("brands total") {
        brandCache["all"]?.let { cached ->
            Log.d(TAG, "brands cache hit size=${cached.size}")
            return@logTimed cached
        }

        val brands = coroutineScope {
            val vendorBrandsDeferred = async {
                logTimed("GET products for vendor brands") {
                    apiService.getProducts().products.vendorBrands()
                }
            }
            val dashboardBrandsDeferred = async {
                logTimed("GET custom_collections brands") {
                    apiService.getCustomCollections().custom_collections.dashboardBrands()
                }
            }

            val currentVendorBrands = vendorBrandsDeferred.await()
            val dashboardBrandsWithProducts = dashboardBrandsDeferred.await()
                .withVendorImages(currentVendorBrands)

            if (dashboardBrandsWithProducts.isNotEmpty()) dashboardBrandsWithProducts else currentVendorBrands
        }

        brandCache["all"] = brands
        brands
    }

    override suspend fun getProducts(vendor: String?): List<ProductDto> {
        return fetchAllProducts(vendor = vendor, collectionId = null)
    }

    override suspend fun getProductsPreview(limit: Int): List<ProductDto> = logTimed(
        "products preview total limit=$limit",
    ) {
        val safeLimit = limit.coerceIn(1, SHOPIFY_MAX_PAGE_SIZE)
        val cacheKey = "preview:$safeLimit"
        productListCache[cacheKey]?.let { cached ->
            Log.d(TAG, "products preview cache hit limit=$safeLimit size=${cached.size}")
            return@logTimed cached
        }

        val page = logTimed("GET products preview limit=$safeLimit") {
            apiService.getProducts(limit = safeLimit).products
        }
        val localizationByProductId = fetchLocalizations(page)
        page.map { product ->
            product.toProductDto(localizationByProductId[product.id] ?: ProductLocalizationFields())
        }.distinctBy { it.id }.also { products ->
            productListCache[cacheKey] = products
        }
    }

    override suspend fun getProductsByIds(productIds: List<Long>): List<ProductDto> = logTimed(
        "products by ids total count=${productIds.distinct().size}",
    ) {
        if (productIds.isEmpty()) return@logTimed emptyList()
        val distinctIds = productIds.distinct()
        val cacheKey = "ids:${distinctIds.sorted().joinToString(",")}"
        productListCache[cacheKey]?.let { cached ->
            Log.d(TAG, "products by ids cache hit size=${cached.size}")
            return@logTimed cached
        }

        val response = logTimed("GET products by ids count=${distinctIds.size}") {
            apiService.getProductsByIds(distinctIds.joinToString(","))
        }
        val localizationByProductId = fetchLocalizations(response.products)
        val products = response.products.map { product ->
            product.toProductDto(localizationByProductId[product.id] ?: ProductLocalizationFields())
        }
        productListCache[cacheKey] = products
        products
    }

    override suspend fun getProducts(collectionId: Long?): List<ProductDto> {
        return fetchAllProducts(vendor = null, collectionId = collectionId)
    }

    private suspend fun fetchAllProducts(
        vendor: String?,
        collectionId: Long?,
    ): List<ProductDto> = logTimed("products total key=${productCacheKey(vendor, collectionId)}") {
        val cacheKey = productCacheKey(vendor, collectionId)
        productListCache[cacheKey]?.let { cached ->
            Log.d(TAG, "products cache hit key=$cacheKey size=${cached.size}")
            return@logTimed cached
        }

        val products = mutableListOf<ProductDto>()
        var sinceId: Long? = null
        var pageIndex = 1

        do {
            val page = logTimed("GET products page=$pageIndex key=$cacheKey sinceId=$sinceId") {
                apiService
                    .getProducts(
                        vendor = vendor,
                        collectionId = collectionId,
                        sinceId = sinceId,
                        limit = SHOPIFY_MAX_PAGE_SIZE,
                    )
                    .products
            }
            if (page.isEmpty()) break

            val localizationByProductId = fetchLocalizations(page)
            products += page.map { product ->
                product.toProductDto(localizationByProductId[product.id] ?: ProductLocalizationFields())
            }
            sinceId = page.maxOfOrNull { it.id }
            pageIndex++
        } while (page.size == SHOPIFY_MAX_PAGE_SIZE && sinceId != null)

        products.distinctBy { it.id }.also { productListCache[cacheKey] = it }
    }

    private suspend fun fetchLocalizations(
        products: List<ShopifyProduct>,
    ): Map<Long, ProductLocalizationFields> = logTimed("localization total count=${products.distinctBy { it.id }.size}") {
        val productIds = products.distinctBy { it.id }.map { it.id }
        val cached = productIds.mapNotNull { productId ->
            localizationCache[productId]?.let { productId to it }
        }.toMap()
        val missingProductIds = productIds.filterNot { localizationCache.containsKey(it) }

        if (missingProductIds.isEmpty()) {
            Log.d(TAG, "localization cache hit count=${cached.size}")
            return@logTimed cached
        }

        val fetched = coroutineScope {
            missingProductIds
                .chunked(LOCALIZATION_BATCH_SIZE)
                .map { chunk -> async { fetchLocalizationChunk(chunk) } }
                .awaitAll()
                .fold(mutableMapOf<Long, ProductLocalizationFields>()) { acc, chunkMap ->
                    acc.apply { putAll(chunkMap) }
                }
        }

        cached + fetched
    }

    private suspend fun fetchLocalizationChunk(
        productIds: List<Long>,
    ): Map<Long, ProductLocalizationFields> = localizationSemaphore.withPermit {
        logTimed("POST graphql localization batch count=${productIds.size}") {
            val request = ShopifyGraphQlRequest(
                query = PRODUCT_LOCALIZATION_QUERY.trimIndent(),
                variables = ProductLocalizationGraphQlVariables(
                    ids = productIds.map { it.toShopifyProductGid() },
                    namespace = LOCALIZATION_NAMESPACE,
                ),
            )

            val response = try {
                apiService.getProductLocalizationMetafields(request)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load localization metafields batch size=${productIds.size}", e)
                return@logTimed productIds.associateWithEmptyLocalization()
            }

            if (response.errors.isNotEmpty()) {
                Log.w(
                    TAG,
                    "GraphQL localization errors: ${response.errors.joinToString { it.message.orEmpty() }}",
                )
            }

            val fetched = response.data
                ?.nodes
                .orEmpty()
                .mapNotNull { node ->
                    val productId = node?.legacyResourceId?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: return@mapNotNull null
                    productId to node.metafields?.nodes.orEmpty().toGraphQlProductLocalizationFields()
                }
                .toMap()

            productIds.associateWith { productId ->
                fetched[productId] ?: ProductLocalizationFields()
            }.also { localizationByProductId ->
                localizationByProductId.forEach { (productId, localization) ->
                    localizationCache[productId] = localization
                }
            }
        }
    }

    private suspend fun fetchProductLocalization(productId: Long): ProductLocalizationFields {
        localizationCache[productId]?.let { return it }
        return fetchLocalizationChunk(listOf(productId))[productId] ?: ProductLocalizationFields()
    }

    override suspend fun getProductDetail(productId: Long): ShopifyProductDetail = logTimed(
        "product detail total productId=$productId",
    ) {
        productDetailCache[productId]?.let { cached ->
            Log.d(TAG, "product detail cache hit productId=$productId")
            return@logTimed cached
        }

        val response = logTimed("GET product detail productId=$productId") {
            apiService.getProductDetail(productId)
        }
        response.product.copy(
            localization = fetchProductLocalization(response.product.id),
        ).also { productDetail ->
            productDetailCache[productId] = productDetail
        }
    }

    private suspend fun <T> logTimed(
        operation: String,
        block: suspend () -> T,
    ): T {
        val startMs = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            Log.d(TAG, "$operation took ${SystemClock.elapsedRealtime() - startMs}ms")
        }
    }

    private fun productCacheKey(vendor: String?, collectionId: Long?): String = when {
        vendor != null -> "vendor:${vendor.trim().lowercase()}"
        collectionId != null -> "collection:$collectionId"
        else -> "all"
    }

    private fun Long.toShopifyProductGid(): String = "gid://shopify/Product/$this"

    private fun List<Long>.associateWithEmptyLocalization(): Map<Long, ProductLocalizationFields> =
        associateWith { ProductLocalizationFields() }.also { localizationByProductId ->
            localizationByProductId.forEach { (productId, localization) ->
                localizationCache[productId] = localization
            }
        }
}
