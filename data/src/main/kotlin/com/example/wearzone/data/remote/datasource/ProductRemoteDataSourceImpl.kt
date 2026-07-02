package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.ProductApiService
import com.example.wearzone.data.remote.dto.BrandDto
import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.ProductDto
import com.example.wearzone.data.remote.dto.ShopifyProductDetail
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val apiService: ProductApiService
) : IProductRemoteDataSource {

    override suspend fun getCategories(): List<CategoryDto> {
        val response = apiService.getCustomCollections()
        return response.custom_collections.map { it.toCategoryDto() }
    }

    override suspend fun getBrands(): List<BrandDto> {
        val response = apiService.getSmartCollections()
        return response.smart_collections.map { it.toBrandDto() }
    }

    override suspend fun getProducts(vendor: String?): List<ProductDto> {
        val response = apiService.getProducts(vendor)
        return response.products.map { it.toProductDto() }
    }

    override suspend fun getProductsByIds(productIds: List<Long>): List<ProductDto> {
        if (productIds.isEmpty()) return emptyList()
        val response = apiService.getProductsByIds(productIds.distinct().joinToString(","))
        return response.products.map { it.toProductDto() }
    }

    override suspend fun getProductDetail(productId: Long): ShopifyProductDetail {
        val response = apiService.getProductDetail(productId)
        return response.product
    }
}
