package com.example.wearzone.domain.checkout.model

enum class CheckoutPaymentMethod {
    CashOnDelivery,
    CreditCard,
}

class MissingCheckoutAddressException : Exception()
