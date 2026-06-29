package com.example.wearzone.presentation.product.detail

sealed interface ProductDetailUiEffect {
    data class ShowToast(val message: String) : ProductDetailUiEffect
    data object NavigateToCart : ProductDetailUiEffect
    // JETS Lab Requirement 1 & 4 — Guest restriction
    data object ShowAuthRequiredError : ProductDetailUiEffect
}
