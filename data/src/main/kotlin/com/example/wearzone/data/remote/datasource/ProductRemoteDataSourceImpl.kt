package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.ProductApiService
import com.example.wearzone.data.remote.dto.BrandDto
import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.ProductDto
import com.example.wearzone.data.remote.dto.ShopifyProductDetail
import com.example.wearzone.data.remote.dto.dashboardBrands
import com.example.wearzone.data.remote.dto.dashboardCategories
import com.example.wearzone.data.remote.dto.vendorBrands
import com.example.wearzone.data.remote.dto.withVendorImages
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val apiService: ProductApiService,
) : IProductRemoteDataSource {

    private companion object {
        const val SHOPIFY_MAX_PAGE_SIZE = 250
    }

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
        return response.products.map { it.toProductDto() }
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
            products += page.map { it.toProductDto() }
            sinceId = page.maxOfOrNull { it.id }
        } while (page.size == SHOPIFY_MAX_PAGE_SIZE && sinceId != null)

        return products.distinctBy { it.id }
    }

    override suspend fun getProductDetail(productId: Long): ShopifyProductDetail {
        val response = apiService.getProductDetail(productId)
        return response.product
    }
}
