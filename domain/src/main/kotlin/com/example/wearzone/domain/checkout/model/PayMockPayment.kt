package com.example.wearzone.domain.checkout.model

data class PayMockPaymentResponse(
    val id: String,
    val status: String,
    val amount: Double,
    val currency: String,
)
