package com.example.wearzone.domain.account.model

data class OrderDetails(
    val id: Long,
    val name: String?,
    val orderNumber: Long?,
    val createdAt: String?,
    val cancelledAt: String?,
    val closedAt: String?,
    val cancelReason: String?,
    val totalPrice: Double,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val taxPrice: Double,
    val currencyCode: String,
    val financialStatus: String?,
    val fulfillmentStatus: String?,
    val orderStatus: OrderStatus,
    val canCancel: Boolean,
    val lineItems: List<OrderDetailsLineItem>,
    val shippingAddress: OrderDetailsAddress?,
    val paymentMethods: List<String>,
    val trackingNumber: String?,
    val trackingUrl: String?,
)

data class OrderDetailsLineItem(
    val id: Long,
    val productId: Long?,
    val variantId: Long?,
    val title: String,
    val variantTitle: String?,
    val quantity: Int,
    val price: Double,
    val imageUrl: String?,
)

data class OrderDetailsAddress(
    val name: String?,
    val firstName: String?,
    val lastName: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val province: String?,
    val country: String?,
    val zip: String?,
    val phone: String?,
)

enum class OrderCancelReason(val value: String) {
    Customer("customer"),
    Inventory("inventory"),
    Fraud("fraud"),
    Declined("declined"),
    Other("other"),
}

