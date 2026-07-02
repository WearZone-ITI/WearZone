package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrdersResponseDto(
    val orders: List<OrderDto> = emptyList(),
)

@Serializable
data class ShopifyOrderResponseDto(
    val order: OrderDto,
)

@Serializable
data class ShopifyOrderRequestDto(
    val order: ShopifyOrderPayloadDto,
)

@Serializable
data class ShopifyOrderPayloadDto(
    @SerialName("line_items")
    val lineItems: List<ShopifyOrderLineItemRequestDto>,
    val customer: ShopifyOrderCustomerDto,
    @SerialName("shipping_address")
    val shippingAddress: ShopifyOrderShippingAddressDto? = null,
    @SerialName("discount_codes")
    val discountCodes: List<ShopifyOrderDiscountCodeDto>? = null,
    val note: String? = null,
    @SerialName("financial_status")
    val financialStatus: String = "pending",
    @SerialName("inventory_behaviour")
    val inventoryBehaviour: String = "decrement_obeying_policy",
)

@Serializable
data class ShopifyOrderLineItemRequestDto(
    @SerialName("variant_id")
    val variantId: Long,
    val quantity: Int,
)

@Serializable
data class ShopifyOrderCustomerDto(
    val id: Long,
)

@Serializable
data class ShopifyOrderShippingAddressDto(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val zip: String? = null,
    val phone: String? = null,
)

@Serializable
data class OrderDto(
    val id: Long,
    val name: String? = null,
    @SerialName("order_number")
    val orderNumber: Long? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("total_price")
    val totalPrice: String? = null,
    val currency: String? = null,
    @SerialName("financial_status")
    val financialStatus: String? = null,
    @SerialName("fulfillment_status")
    val fulfillmentStatus: String? = null,
    @SerialName("cancelled_at")
    val cancelledAt: String? = null,
    @SerialName("closed_at")
    val closedAt: String? = null,
    @SerialName("line_items")
    val lineItems: List<OrderLineItemDto> = emptyList(),
    val fulfillments: List<OrderFulfillmentDto> = emptyList(),
)

@Serializable
data class OrderLineItemDto(
    val id: Long,
    @SerialName("product_id")
    val productId: Long? = null,
    @SerialName("variant_id")
    val variantId: Long? = null,
    val title: String? = null,
    val name: String? = null,
    val quantity: Int = 0,
    val price: String? = null,
    val image: OrderLineItemImageDto? = null,
)

@Serializable
data class OrderLineItemImageDto(
    val src: String? = null,
)

@Serializable
data class OrderFulfillmentDto(
    @SerialName("tracking_number")
    val trackingNumber: String? = null,
    @SerialName("tracking_url")
    val trackingUrl: String? = null,
    @SerialName("tracking_urls")
    val trackingUrls: List<String> = emptyList(),
)
