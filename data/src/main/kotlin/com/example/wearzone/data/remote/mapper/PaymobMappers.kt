package com.example.wearzone.data.remote.mapper

import com.example.data.BuildConfig
import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.PaymentIntention

private const val CURRENCY_MULTIPLIER = 100L

fun CheckoutData.toIntentionRequestDto(specialRef: String): com.example.wearzone.data.remote.dto.IntentionRequestDto {

    val intentionItems = cartItems.map { item ->
        _root_ide_package_.com.example.wearzone.data.remote.dto.IntentionItemDto(
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

    return _root_ide_package_.com.example.wearzone.data.remote.dto.IntentionRequestDto(
        amount = totalAmount,
        paymentMethods = listOf(BuildConfig.PAYMOB_INTEGRATION_ID.toLong()),
        items = intentionItems,
        billingData = _root_ide_package_.com.example.wearzone.data.remote.dto.BillingDataDto(
            firstName = customerInfo.firstName,
            lastName = customerInfo.lastName,
            phoneNumber = customerInfo.phone,
            email = customerInfo.email
        ),
        customer = _root_ide_package_.com.example.wearzone.data.remote.dto.CustomerPaymentDto(
            firstName = customerInfo.firstName,
            lastName = customerInfo.lastName,
            email = customerInfo.email
        ),
        specialReference = specialRef,
        extras = emptyMap()
    )
}

fun com.example.wearzone.data.remote.dto.IntentionResponseDto.toDomain(): PaymentIntention {
    return PaymentIntention(
        intentionId = id,
        clientSecret = clientSecret,
        amount = intentionDetail?.amount ?: 0L,
        currency = intentionDetail?.currency ?: "EGP",
        status = status
    )
}