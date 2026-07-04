package com.example.wearzone.data.remote.dto

import com.example.wearzone.domain.checkout.model.PayMockPaymentResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PayMockProjectRequestDto(
    val name: String,
)

@Serializable
data class PayMockProjectResponseDto(
    val id: String,
    @SerialName("object")
    val objectType: String,
    val name: String? = null,
    @SerialName("api_key")
    val apiKey: String,
    @SerialName("webhook_url")
    val webhookUrl: String? = null,
    val created: Long? = null
)

@Serializable
data class PayMockPaymentRequestDto(
    val amount: Double,
    val currency: String,
    val method: String = "credit_card",
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("customer_email")
    val customerEmail: String? = null,
)

@Serializable
data class PayMockPaymentResponseDto(
    val id: String,
    @SerialName("object")
    val objectType: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val method: String? = null,
    val description: String? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("customer_email")
    val customerEmail: String? = null,
    @SerialName("failure_reason")
    val failureReason: String? = null,
    @SerialName("simulation_rule")
    val simulationRule: String? = null,
    val created: Long? = null,
    val pix: PayMockPixDto? = null,
)

@Serializable
data class PayMockPixDto(
    @SerialName("qr_code_url")
    val qrCodeUrl: String? = null,
)

fun PayMockPaymentResponseDto.toDomain(): PayMockPaymentResponse =
    PayMockPaymentResponse(
        id = id,
        status = status,
        amount = amount,
        currency = currency,
    )
