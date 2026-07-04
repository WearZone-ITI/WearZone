package com.example.wearzone.presentation.order.details

sealed interface OrderDetailsUiIntent {
    data class LoadOrder(val orderId: Long) : OrderDetailsUiIntent
    data object OnBackClicked : OrderDetailsUiIntent
    data object OnRetry : OrderDetailsUiIntent
    data object OnCancelOrderClicked : OrderDetailsUiIntent
    data object OnCancelConfirmed : OrderDetailsUiIntent
    data object OnContinueShoppingClicked : OrderDetailsUiIntent
}

