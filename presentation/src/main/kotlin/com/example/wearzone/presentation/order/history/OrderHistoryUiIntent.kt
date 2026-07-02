package com.example.wearzone.presentation.order.history

sealed interface OrderHistoryUiIntent {
    data object OnBackClicked : OrderHistoryUiIntent
    data object OnRetry : OrderHistoryUiIntent
    data object OnRefresh : OrderHistoryUiIntent
    data class OnViewDetailsClicked(val orderId: Long) : OrderHistoryUiIntent
    data class OnTrackPackageClicked(val orderId: Long) : OrderHistoryUiIntent
}
