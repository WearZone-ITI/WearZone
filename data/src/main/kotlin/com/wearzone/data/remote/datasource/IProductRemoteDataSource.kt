package com.wearzone.data.remote.datasource

import com.wearzone.data.remote.dto.BrandDto
import com.wearzone.data.remote.dto.CategoryDto
import com.wearzone.data.remote.dto.ProductDto

interface IProductRemoteDataSource {
    suspend fun getCategories(): List<CategoryDto>
    suspend fun getBrands(): List<BrandDto>
    suspend fun getProducts(): List<ProductDto>
}
