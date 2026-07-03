package com.example.wearzone.presentation.wishlist

sealed interface WishlistUiEffect {
    data class NavigateToProductDetail(val productId: String) : WishlistUiEffect
    data object NavigateToCart : WishlistUiEffect
    data class ShowSnackbar(@androidx.annotation.StringRes val message: Int) : WishlistUiEffect
}
