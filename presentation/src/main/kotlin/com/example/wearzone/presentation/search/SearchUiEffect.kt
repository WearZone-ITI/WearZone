package com.example.wearzone.presentation.search

sealed interface SearchUiEffect {
    data class NavigateToProductDetail(val productId: String) : SearchUiEffect
}
