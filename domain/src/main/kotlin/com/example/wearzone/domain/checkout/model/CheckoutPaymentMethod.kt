package com.example.wearzone.domain.checkout.model

enum class CheckoutPaymentMethod {
    CashOnDelivery,
}

class MissingCheckoutAddressException : Exception()
