package com.example.wearzone.presentation.product.detail

sealed interface ProductDetailUiIntent {
    data object LoadProduct : ProductDetailUiIntent
    data object Retry : ProductDetailUiIntent
    data class SelectSize(val size: String) : ProductDetailUiIntent
    data object OnToggleFavorite : ProductDetailUiIntent
    data object OnConfirmRemove : ProductDetailUiIntent
    data object OnCancelRemove : ProductDetailUiIntent
    data object OnAddToCartClick : ProductDetailUiIntent
    data class SubmitReview(val rating: Double, val comment: String) : ProductDetailUiIntent
    data object OnWriteReviewClick : ProductDetailUiIntent
}
