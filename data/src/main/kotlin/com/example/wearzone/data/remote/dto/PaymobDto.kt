package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymobAuthRequest(
    @SerialName("api_key") val apiKey: String
)

@Serializable
data class PaymobAuthResponse(
    val token: String
)

@Serializable
data class PaymobOrderRequest(
    @SerialName("auth_token") val authToken: String,
    @SerialName("delivery_needed") val deliveryNeeded: Boolean = false,
    @SerialName("amount_cents") val amountCents: Long,
    val currency: String,
    val items: List<PaymobOrderItemDto> = emptyList()
)

@Serializable
data class PaymobOrderItemDto(
    val name: String,
    @SerialName("amount_cents") val amountCents: Long,
    val description: String = "",
    val quantity: Int = 1
)

@Serializable
data class PaymobOrderResponse(
    val id: Long,
    @SerialName("merchant_order_id") val merchantOrderId: String? = null
)

@Serializable
data class PaymobPaymentKeyRequest(
    @SerialName("auth_token") val authToken: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("expiration") val expiration: Int = 3600,
    @SerialName("order_id") val orderId: Long,
    @SerialName("billing_data") val billingData: PaymobBillingDataDto,
    val currency: String,
    @SerialName("integration_id") val integrationId: Int
)

@Serializable
data class PaymobBillingDataDto(
    val apartment: String = "NA",
    val email: String,
    val floor: String = "NA",
    val first_name: String,
    val street: String = "NA",
    val building: String = "NA",
    val phone_number: String,
    val shipping_method: String = "PKG",
    val postal_code: String = "NA",
    val city: String = "NA",
    val country: String = "NA",
    val last_name: String,
    val state: String = "NA"
)

@Serializable
data class PaymobPaymentKeyResponse(
    val token: String
)

@Serializable
data class PaymobDirectPayRequest(
    val source: PaymobPaymentSourceDto,
    @SerialName("payment_token") val paymentToken: String
)

@Serializable
data class PaymobPaymentSourceDto(
    val identifier: String, // "card"
    val subtype: String,    // "CARD"
    @SerialName("card_info") val cardInfo: PaymobCardInfoDto
)

@Serializable
data class PaymobCardInfoDto(
    val pan: String,
    val cvv: String,
    @SerialName("expiry_month") val expiryMonth: String,
    @SerialName("expiry_year") val expiryYear: String,
    @SerialName("holder_name") val holderName: String
)

@Serializable
data class PaymobDirectPayResponse(
    val id: Long,
    val pending: Boolean,
    val success: Boolean,
    @SerialName("is_3d_secure") val is3dSecure: Boolean,
    @SerialName("redirection_url") val redirectionUrl: String? = null
)
@Serializable
data class PaymobIntentionRequest(
    val amount: Long, // بالسنت
    val currency: String,
    @SerialName("payment_methods") val paymentMethods: List<Int>,
    val items: List<PaymobIntentionItemDto> = emptyList(),
    @SerialName("billing_data") val billingData: PaymobBillingDataDto,
)

@Serializable
data class PaymobIntentionItemDto(
    val name: String,
    val amount: Long,
    val description: String = "",
    val quantity: Int = 1,
)

@Serializable
data class PaymobIntentionResponse(
    @SerialName("client_secret") val clientSecret: String,
)