package com.example.wearzone.domain.search.model

data class SearchFilters(
    val query: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val brandTitle: String? = null,
    val categoryTitle: String? = null,
)
