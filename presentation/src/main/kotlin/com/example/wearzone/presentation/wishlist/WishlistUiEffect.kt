package com.example.wearzone.presentation.wishlist

sealed interface WishlistUiEffect {
    data class NavigateToProductDetail(val productId: String) : WishlistUiEffect
    data class ShowSnackbar(val message: String) : WishlistUiEffect
}
