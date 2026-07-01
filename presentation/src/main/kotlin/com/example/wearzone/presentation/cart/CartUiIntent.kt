package com.example.wearzone.presentation.cart

sealed interface CartUiIntent {
    data object OnRetry : CartUiIntent
    data object OnCheckoutClicked : CartUiIntent
    data object OnClearCartClicked : CartUiIntent
    data object OnClearCartConfirmed : CartUiIntent
    data class OnIncreaseQuantity(val variantId: String) : CartUiIntent
    data class OnDecreaseQuantity(val variantId: String) : CartUiIntent
    data class OnRemoveItemClicked(val variantId: String) : CartUiIntent
    data class OnRemoveItemConfirmed(val variantId: String) : CartUiIntent
}
