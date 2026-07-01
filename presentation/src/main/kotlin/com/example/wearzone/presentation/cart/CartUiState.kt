package com.example.wearzone.presentation.cart

import kotlinx.collections.immutable.ImmutableList

sealed interface CartUiState {
    data object Loading : CartUiState
    data object LoginRequired : CartUiState
    data object Empty : CartUiState
    data class Content(
        val items: ImmutableList<CartItemUiModel>,
        val itemCount: Int,
        val subtotal: String,
        val total: String,
    ) : CartUiState
    data class Error(val message: String) : CartUiState
}

data class CartItemUiModel(
    val variantId: String,
    val productId: String,
    val title: String,
    val vendor: String,
    val price: String,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String?,
    val size: String?,
    val isLowStock: Boolean,
)
