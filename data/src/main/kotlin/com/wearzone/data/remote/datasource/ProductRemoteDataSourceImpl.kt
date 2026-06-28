package com.wearzone.data.remote.datasource

import com.wearzone.data.remote.api.ProductApiService
import com.wearzone.data.remote.dto.BrandDto
import com.wearzone.data.remote.dto.CategoryDto
import com.wearzone.data.remote.dto.ProductDto
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

    override suspend fun getProducts(): List<ProductDto> {
        val response = apiService.getProducts()
        return response.products.map { it.toProductDto() }
    }
}
