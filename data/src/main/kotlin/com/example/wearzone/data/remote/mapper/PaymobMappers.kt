package com.example.wearzone.data.remote.dto

import android.icu.number.Precision.currency
import com.example.data.BuildConfig
import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.PaymentIntention

private const val CURRENCY_MULTIPLIER = 100L

fun CheckoutData.toIntentionRequestDto(specialRef: String): IntentionRequestDto {

    val intentionItems = cartItems.map { item ->
        IntentionItemDto(
            name = item.title,
            amount = (item.price * CURRENCY_MULTIPLIER).toLong(),
            description = item.size?.let { "Size: $it" } ?: item.vendor,
            quantity = item.quantity
        )
    }

    val itemsTotal = intentionItems.sumOf { it.amount * it.quantity }
    check(itemsTotal == totalAmount) {
        "Amount mismatch: totalAmount=$totalAmount but items sum=$itemsTotal"
    }

    return IntentionRequestDto(
        amount = totalAmount,
        paymentMethods = listOf(BuildConfig.PAYMOB_INTEGRATION_ID.toLong()),
        items = intentionItems,
        billingData = BillingDataDto(
            firstName = customerInfo.firstName,
            lastName = customerInfo.lastName,
            phoneNumber = "01279336697",
            email = customerInfo.email
        ),
        customer = CustomerPaymentDto(
            firstName = customerInfo.firstName,
            lastName = customerInfo.lastName,
            email = customerInfo.email
        ),
        specialReference = specialRef,
        extras = emptyMap()
    )
}

private fun String.toPaymobPhoneFormat(): String {
    if (this.startsWith("+")) return this
    val digitsOnly = this.trim().removePrefix("0")
    return "+20$digitsOnly"
}

fun IntentionResponseDto.toDomain(): PaymentIntention {
    return PaymentIntention(
        intentionId = id,
        clientSecret = clientSecret,
        amount = intentionDetail?.amount ?: 0L,
        currency = intentionDetail?.currency ?: "EGP",
        status = status
    )
}