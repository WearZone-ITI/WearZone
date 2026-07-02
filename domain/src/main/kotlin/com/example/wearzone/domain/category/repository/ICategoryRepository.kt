package com.example.wearzone.domain.category.repository

import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DataResult

interface ICategoryRepository {
    suspend fun getCategories(): DataResult<List<Category>>
}
