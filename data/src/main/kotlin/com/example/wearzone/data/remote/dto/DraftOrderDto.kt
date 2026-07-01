package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DraftOrderRequest(
    @SerialName("draft_order") val draftOrder: DraftOrderPayload
)

@Serializable
data class DraftOrderPayload(
    @SerialName("line_items") val lineItems: List<DraftOrderLineItem>,
    @SerialName("customer") val customer: DraftOrderCustomer? = null,
    @SerialName("use_customer_default_address") val useCustomerDefaultAddress: Boolean? = true
)

@Serializable
data class DraftOrderLineItem(
  //  @SerialName("title") val title: String? = null,
  //  @SerialName("price") val price: String? = null,
    @SerialName("quantity") val quantity: Int,
    @SerialName("variant_id") val variantId: Long? = null,
//    @SerialName("product_id") val productId: Long? = null
)

@Serializable
data class DraftOrderCustomer(
    @SerialName("id") val id: Long
)

@Serializable
data class DraftOrderResponse(
    @SerialName("draft_order") val draftOrder: DraftOrderDto
)

@Serializable
data class DraftOrderDto(
    @SerialName("id") val id: Long,
    @SerialName("line_items") val lineItems: List<DraftOrderLineItemDto> = emptyList()
)

@Serializable
data class DraftOrderLineItemDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("variant_id") val variantId: Long? = null,
    @SerialName("product_id") val productId: Long? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price") val price: String? = null
)
