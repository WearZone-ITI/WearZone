package com.example.wearzone.presentation.product.detail

import androidx.annotation.StringRes

sealed interface ProductDetailUiEffect {
    data class ShowToast(@StringRes val messageRes: Int) : ProductDetailUiEffect
    data class ShowSignInRequired(@StringRes val messageRes: Int) : ProductDetailUiEffect
    data object OpenWriteReviewSheet : ProductDetailUiEffect
}
