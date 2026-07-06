package com.example.wearzone.presentation.order.details

import androidx.annotation.StringRes
import com.example.wearzone.presentation.order.history.OrderStatusTone
import kotlinx.collections.immutable.ImmutableList

sealed interface OrderDetailsUiState {
    data object Loading : OrderDetailsUiState
    data object SignInRequired : OrderDetailsUiState

    data class Content(
        val order: OrderDetailsUiModel,
        val isRefreshing: Boolean = false,
        val isCanceling: Boolean = false,
    ) : OrderDetailsUiState

    data class Error(@param:StringRes val messageRes: Int) : OrderDetailsUiState
}

data class OrderDetailsUiModel(
    val id: Long,
    val displayName: String,
    val placedDate: String,
    val status: OrderDetailsStatusUiModel,
    val timeline: ImmutableList<OrderTimelineUiModel>,
    val items: ImmutableList<OrderDetailsItemUiModel>,
    val shippingAddress: OrderDetailsAddressUiModel?,
    val paymentMethods: ImmutableList<String>,
    val subtotalAmount: Double,
    val shippingAmount: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val canCancel: Boolean,
)

data class OrderDetailsStatusUiModel(
    val label: String? = null,
    @param:StringRes val labelRes: Int? = null,
    val tone: OrderStatusTone,
)

data class OrderTimelineUiModel(
    @param:StringRes val labelRes: Int,
    val description: String,
    val tone: OrderStatusTone,
    val isActive: Boolean,
)

data class OrderDetailsItemUiModel(
    val id: Long,
    val title: String,
    val variantInfo: String,
    val quantity: Int,
    val basePriceEgp: Double,
    val imageUrl: String?,
)

data class OrderDetailsAddressUiModel(
    val recipientName: String,
    val addressLines: String,
    val phone: String?,
)
