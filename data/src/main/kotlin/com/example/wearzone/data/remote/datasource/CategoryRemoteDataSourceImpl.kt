package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.ProductApiService
import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.dashboardCategories
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRemoteDataSourceImpl @Inject constructor(
    private val api: ProductApiService,
) : ICategoryRemoteDataSource {

    @Volatile
    private var categoriesCache: List<CategoryDto>? = null

    override suspend fun fetchCategories(): List<CategoryDto> {
        categoriesCache?.let { return it }
        return api.getCustomCollections().custom_collections.dashboardCategories()
            .also { categoriesCache = it }
    }
}
