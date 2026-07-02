package com.example.wearzone.presentation.checkout

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList

sealed interface CheckoutUiState {
    data object Loading : CheckoutUiState
    data object Empty : CheckoutUiState

    data class Content(
        val items: ImmutableList<CheckoutCartItemUiModel>,
        val itemCount: Int,
        val subtotal: String,
        val total: String,
        val isPlacingOrder: Boolean = false,
    ) : CheckoutUiState

    data class Error(@param:StringRes val messageRes: Int) : CheckoutUiState
}

data class CheckoutCartItemUiModel(
    val variantId: String,
    val title: String,
    val quantity: Int,
    val formattedPrice: String,
)
