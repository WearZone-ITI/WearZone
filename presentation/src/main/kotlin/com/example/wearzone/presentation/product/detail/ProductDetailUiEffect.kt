package com.example.wearzone.presentation.product.detail

sealed interface ProductDetailUiEffect {
    data class ShowToast(@androidx.annotation.StringRes val messageRes: Int) : ProductDetailUiEffect
    // JETS Lab Requirement 1 & 4 — Guest restriction
    data object ShowSignInRequired : ProductDetailUiEffect
}
