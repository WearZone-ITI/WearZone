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
data class ShopifyCancelOrderRequestDto(
    val reason: String = "customer",
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
    @SerialName("subtotal_price")
    val subtotalPrice: String? = null,
    @SerialName("current_subtotal_price")
    val currentSubtotalPrice: String? = null,
    @SerialName("total_tax")
    val totalTax: String? = null,
    @SerialName("current_total_tax")
    val currentTotalTax: String? = null,
    @SerialName("total_shipping_price_set")
    val totalShippingPriceSet: OrderPriceSetDto? = null,
    val currency: String? = null,
    val customer: OrderCustomerDto? = null,
    @SerialName("shipping_address")
    val shippingAddress: OrderAddressDto? = null,
    @SerialName("payment_gateway_names")
    val paymentGatewayNames: List<String> = emptyList(),
    @SerialName("financial_status")
    val financialStatus: String? = null,
    @SerialName("fulfillment_status")
    val fulfillmentStatus: String? = null,
    @SerialName("cancelled_at")
    val cancelledAt: String? = null,
    @SerialName("cancel_reason")
    val cancelReason: String? = null,
    @SerialName("closed_at")
    val closedAt: String? = null,
    @SerialName("line_items")
    val lineItems: List<OrderLineItemDto> = emptyList(),
    val fulfillments: List<OrderFulfillmentDto> = emptyList(),
)

@Serializable
data class OrderCustomerDto(
    val id: Long? = null,
)

@Serializable
data class OrderAddressDto(
    val name: String? = null,
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
data class OrderPriceSetDto(
    @SerialName("shop_money")
    val shopMoney: OrderMoneyDto? = null,
)

@Serializable
data class OrderMoneyDto(
    val amount: String? = null,
    @SerialName("currency_code")
    val currencyCode: String? = null,
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
    @SerialName("variant_title")
    val variantTitle: String? = null,
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
    val status: String? = null,
    @SerialName("shipment_status")
    val shipmentStatus: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("tracking_number")
    val trackingNumber: String? = null,
    @SerialName("tracking_url")
    val trackingUrl: String? = null,
    @SerialName("tracking_urls")
    val trackingUrls: List<String> = emptyList(),
)
