package com.example.wearzone.presentation.checkout

data class CheckoutDeliveryAddressUiModel(
    val id: Long,
    val recipientName: String,
    val addressLines: String,
    val countryLine: String,
)

enum class CheckoutPaymentMethodUi {
    CashOnDelivery,
    CreditCard,
}

fun CheckoutPaymentMethodUi.labelRes(): Int =
    when (this) {
        CheckoutPaymentMethodUi.CashOnDelivery -> com.example.presentation.R.string.checkout_payment_cash_on_delivery
        CheckoutPaymentMethodUi.CreditCard -> com.example.presentation.R.string.checkout_payment_credit_card
    }

fun CheckoutPaymentMethodUi.descriptionRes(): Int =
    when (this) {
        CheckoutPaymentMethodUi.CashOnDelivery -> com.example.presentation.R.string.checkout_payment_cash_on_delivery_description
        CheckoutPaymentMethodUi.CreditCard -> com.example.presentation.R.string.checkout_payment_credit_card_description
    }
