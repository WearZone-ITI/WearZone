package com.example.wearzone.presentation.product.list

import com.example.wearzone.domain.product.model.Product

data class ProductListUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null
)