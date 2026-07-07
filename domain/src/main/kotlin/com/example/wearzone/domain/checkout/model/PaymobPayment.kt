package com.example.wearzone.domain.checkout.model

import com.example.wearzone.domain.cart.model.CartItem

data class PaymentIntention(
    val intentionId: String,
    val clientSecret: String,
    val amount: Long,
    val currency: String,
    val status: String
)

data class CheckoutData(
    val cartItems: List<CartItem>,
    val totalAmount: Long,
    val customerInfo: CustomerInfo
)

data class CustomerInfo(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)