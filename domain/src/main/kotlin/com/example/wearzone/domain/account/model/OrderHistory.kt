package com.example.wearzone.domain.account.model

data class OrderHistory(
    val id: Long,
    val name: String?,
    val orderNumber: Long?,
    val createdAt: String?,
    val totalPrice: Double,
    val currencyCode: String,
    val financialStatus: String?,
    val fulfillmentStatus: String?,
    val orderStatus: OrderStatus,
    val paymentGatewayNames: List<String>,
    val lineItems: List<OrderHistoryLineItem>,
    val trackingNumber: String?,
    val trackingUrl: String?,
)

data class OrderHistoryLineItem(
    val id: Long,
    val productId: Long?,
    val variantId: Long?,
    val title: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String?,
)

enum class OrderStatus {
    Open,
    Closed,
    Cancelled,
}
