package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.ProductApiService
import com.example.wearzone.data.remote.dto.CategoryDto
import javax.inject.Inject

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val api: ProductApiService,
) : ICategoryRemoteDataSource {

    override suspend fun fetchCategories(): List<CategoryDto> {
        return api.getCustomCollections().custom_collections
          .map {it.toCategoryDto() }
    }
}