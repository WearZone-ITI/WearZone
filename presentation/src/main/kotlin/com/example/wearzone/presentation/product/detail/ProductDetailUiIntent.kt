package com.example.wearzone.presentation.product.detail

sealed interface ProductDetailUiIntent {
    data object LoadProduct : ProductDetailUiIntent
    data object Retry : ProductDetailUiIntent
    data class SelectSize(val size: String) : ProductDetailUiIntent
    // JETS Lab Requirement 1 — gated by auth check in VM
    data object AddToCart : ProductDetailUiIntent
    // JETS Lab Requirement 4 — gated by auth check in VM
    data object OnToggleFavorite : ProductDetailUiIntent
}
