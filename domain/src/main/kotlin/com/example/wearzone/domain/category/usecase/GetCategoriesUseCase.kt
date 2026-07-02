package com.example.wearzone.domain.category.usecase

import com.example.wearzone.domain.category.repository.ICategoryRepository
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DataResult

class GetCategoriesUseCase(
    private val categoryRepository: ICategoryRepository,
) {
    suspend operator fun invoke(): DataResult<List<Category>> = categoryRepository.getCategories()
}
