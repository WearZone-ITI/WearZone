package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.ShopifyCollection

interface ICategoryRemoteDataSource {
    suspend fun fetchCategories(): List<CategoryDto>
}
