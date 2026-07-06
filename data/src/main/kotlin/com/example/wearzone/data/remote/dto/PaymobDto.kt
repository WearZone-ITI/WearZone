package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IntentionRequestDto(
    @SerialName("amount") val amount: Long,
    @SerialName("currency") val currency: String = "EGP",
    @SerialName("payment_methods") val paymentMethods: List<Long>,
    @SerialName("items") val items: List<IntentionItemDto>,
    @SerialName("billing_data") val billingData: BillingDataDto,
    @SerialName("customer") val customer: CustomerPaymentDto,
    @SerialName("extras") val extras: Map<String, String> = emptyMap(),
    @SerialName("special_reference") val specialReference: String
)

@Serializable
data class IntentionItemDto(
    @SerialName("name") val name: String,
    @SerialName("amount") val amount: Long,
    @SerialName("description") val description: String,
    @SerialName("quantity") val quantity: Int
)

@Serializable
data class BillingDataDto(
    @SerialName("apartment") val apartment: String = "NA",
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("street") val street: String = "NA",
    @SerialName("building") val building: String = "NA",
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("city") val city: String = "NA",
    @SerialName("country") val country: String = "EG",
    @SerialName("email") val email: String,
    @SerialName("floor") val floor: String = "NA",
    @SerialName("state") val state: String = "NA"
)

@Serializable
data class CustomerPaymentDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("email") val email: String,
    @SerialName("extras") val extras: Map<String, String> = emptyMap()
)


@Serializable
data class PaymentKeyDto(
    @SerialName("integration") val integration: Long,
    @SerialName("key") val key: String
)@Serializable
data class IntentionResponseDto(
    @SerialName("id")
    val id: String,

    @SerialName("client_secret")
    val clientSecret: String,

    @SerialName("status")
    val status: String,

    @SerialName("payment_keys")
    val paymentKeys: List<PaymentKeyDto> = emptyList(),

    @SerialName("intention_order_id")
    val intentionOrderId: Long? = null,

    @SerialName("intention_detail")
    val intentionDetail: IntentionDetailDto? = null
)

@Serializable
data class IntentionDetailDto(
    @SerialName("amount")
    val amount: Long,

    @SerialName("currency")
    val currency: String
)
