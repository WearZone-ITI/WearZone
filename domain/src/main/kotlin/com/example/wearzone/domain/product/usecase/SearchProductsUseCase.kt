package com.example.wearzone.domain.product.usecase

import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.search.model.SearchFilters

class SearchProductsUseCase(
    private val repository: IProductRepository,
) {
    suspend operator fun invoke(filters: SearchFilters): DataResult<List<Product>> {
        return when (val result = repository.getProducts()) {
            is DataResult.Error -> result
            is DataResult.Success -> DataResult.Success(result.data.filter { it.matches(filters) })
        }
    }

    private fun Product.matches(filters: SearchFilters): Boolean {
        val normalizedQuery = filters.query.trim().lowercase()
        val matchesQuery = normalizedQuery.isBlank() ||
            title.lowercase().contains(normalizedQuery) ||
            vendor.lowercase().contains(normalizedQuery)
        val matchesMinPrice = filters.minPrice?.let { price >= it } ?: true
        val matchesMaxPrice = filters.maxPrice?.let { price <= it } ?: true
        val matchesBrand = filters.brandTitle?.let { vendor.equals(it, ignoreCase = true) } ?: true
        val matchesCategory = filters.categoryTitle?.let { title.contains(it, ignoreCase = true) } ?: true
        return matchesQuery && matchesMinPrice && matchesMaxPrice && matchesBrand && matchesCategory
    }
}
