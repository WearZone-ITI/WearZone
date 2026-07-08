package com.example.wearzone.data.remote.datasource

import android.util.Log
import com.example.wearzone.data.remote.api.ProductApiService
import com.example.wearzone.data.remote.dto.BrandDto
import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.ProductDto
import com.example.wearzone.data.remote.dto.ProductLocalizationFields
import com.example.wearzone.data.remote.dto.ShopifyProduct
import com.example.wearzone.data.remote.dto.ShopifyProductDetail
import com.example.wearzone.data.remote.dto.dashboardBrands
import com.example.wearzone.data.remote.dto.dashboardCategories
import com.example.wearzone.data.remote.dto.toProductLocalizationFields
import com.example.wearzone.data.remote.dto.vendorBrands
import com.example.wearzone.data.remote.dto.withVendorImages
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val apiService: ProductApiService,
) : IProductRemoteDataSource {

    private companion object {
        const val SHOPIFY_MAX_PAGE_SIZE = 250
        const val LOCALIZATION_NAMESPACE = "wearzone"
        const val LOCALIZATION_FETCH_PARALLELISM = 8
        const val TAG = "ProductLocalization"
    }

    private val localizationCache = java.util.concurrent.ConcurrentHashMap<Long, ProductLocalizationFields>()
    private val localizationSemaphore = Semaphore(LOCALIZATION_FETCH_PARALLELISM)

    override suspend fun getCategories(): List<CategoryDto> {
        return apiService.getCustomCollections().custom_collections.dashboardCategories()
    }

    override suspend fun getBrands(): List<BrandDto> {
        val currentVendorBrands = apiService.getProducts().products.vendorBrands()

        val dashboardBrandsWithProducts = apiService
            .getCustomCollections()
            .custom_collections
            .dashboardBrands()
            .withVendorImages(currentVendorBrands)
        if (dashboardBrandsWithProducts.isNotEmpty()) return dashboardBrandsWithProducts

        // Only expose brands backed by at least one current Shopify product.
        // Empty dashboard/smart collections are intentionally ignored so brand clicks never open empty lists.
        return currentVendorBrands
    }

    override suspend fun getProducts(vendor: String?): List<ProductDto> {
        return fetchAllProducts(vendor = vendor, collectionId = null)
    }

    override suspend fun getProductsByIds(productIds: List<Long>): List<ProductDto> {
        if (productIds.isEmpty()) return emptyList()
        val response = apiService.getProductsByIds(productIds.distinct().joinToString(","))
        val localizationByProductId = fetchLocalizations(response.products)
        return response.products.map { product ->
            product.toProductDto(localizationByProductId[product.id] ?: ProductLocalizationFields())
        }
    }

    override suspend fun getProducts(collectionId: Long?): List<ProductDto> {
        return fetchAllProducts(vendor = null, collectionId = collectionId)
    }

    private suspend fun fetchAllProducts(
        vendor: String?,
        collectionId: Long?,
    ): List<ProductDto> {
        val products = mutableListOf<ProductDto>()
        var sinceId: Long? = null

        do {
            val page = apiService
                .getProducts(
                    vendor = vendor,
                    collectionId = collectionId,
                    sinceId = sinceId,
                    limit = SHOPIFY_MAX_PAGE_SIZE,
                )
                .products
            if (page.isEmpty()) break

            val localizationByProductId = fetchLocalizations(page)
            products += page.map { product ->
                product.toProductDto(localizationByProductId[product.id] ?: ProductLocalizationFields())
            }
            sinceId = page.maxOfOrNull { it.id }
        } while (page.size == SHOPIFY_MAX_PAGE_SIZE && sinceId != null)

        return products.distinctBy { it.id }
    }

    private suspend fun fetchLocalizations(
        products: List<ShopifyProduct>,
    ): Map<Long, ProductLocalizationFields> = coroutineScope {
        products.distinctBy { it.id }
            .map { product ->
                async {
                    product.id to fetchProductLocalization(product.id)
                }
            }
            .awaitAll()
            .toMap()
    }

    private suspend fun fetchProductLocalization(productId: Long): ProductLocalizationFields {
        localizationCache[productId]?.let { return it }
        return localizationSemaphore.withPermit {
            localizationCache[productId]?.let { return@withPermit it }
            val localization = try {
                apiService
                    .getProductMetafields(productId = productId, namespace = LOCALIZATION_NAMESPACE)
                    .metafields
                    .toProductLocalizationFields()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load wearzone metafields for product $productId", e)
                ProductLocalizationFields()
            }
            localizationCache[productId] = localization
            localization
        }
    }

    override suspend fun getProductDetail(productId: Long): ShopifyProductDetail {
        val response = apiService.getProductDetail(productId)
        return response.product.copy(
            localization = fetchProductLocalization(response.product.id),
        )
    }
}
