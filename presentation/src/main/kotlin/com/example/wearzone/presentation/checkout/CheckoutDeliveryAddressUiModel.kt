package com.example.wearzone.presentation.checkout

import androidx.annotation.StringRes

data class CheckoutDeliveryAddressUiModel(
    val id: Long,
    val recipientName: String,
    val addressLines: String,
    val countryLine: String,
)

enum class CheckoutPaymentMethodUi {
    CashOnDelivery,
}

fun CheckoutPaymentMethodUi.labelRes(): Int =
    when (this) {
        CheckoutPaymentMethodUi.CashOnDelivery -> com.example.presentation.R.string.checkout_payment_cash_on_delivery
    }

fun CheckoutPaymentMethodUi.descriptionRes(): Int =
    when (this) {
        CheckoutPaymentMethodUi.CashOnDelivery -> com.example.presentation.R.string.checkout_payment_cash_on_delivery_description
    }
