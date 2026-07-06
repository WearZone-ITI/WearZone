package com.example.wearzone.domain.checkout.model

data class PaymobPaymentResponse(
    val id: String,
    val status: String,
    val amount: Double,
    val currency: String,
)
