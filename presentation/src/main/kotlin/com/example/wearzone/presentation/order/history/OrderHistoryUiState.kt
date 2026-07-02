package com.example.wearzone.presentation.order.history

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList

sealed interface OrderHistoryUiState {
    data object Loading : OrderHistoryUiState
    data object Empty : OrderHistoryUiState

    data class Content(
        val orders: ImmutableList<OrderHistoryUiModel>,
        val isRefreshing: Boolean = false,
    ) : OrderHistoryUiState

    data class Error(@param:StringRes val messageRes: Int) : OrderHistoryUiState
}

data class OrderHistoryUiModel(
    val id: Long,
    val displayName: String,
    val placedDate: String,
    val formattedTotalPrice: String,
    val statuses: ImmutableList<OrderStatusUiModel>,
    val thumbnails: ImmutableList<OrderThumbnailUiModel>,
    val canTrack: Boolean,
)

data class OrderThumbnailUiModel(
    val id: Long,
    val imageUrl: String?,
)

data class OrderStatusUiModel(
    val label: String? = null,
    @param:StringRes val labelRes: Int? = null,
    val tone: OrderStatusTone,
)

enum class OrderStatusTone {
    Success,
    Warning,
    Error,
    Neutral,
}
