package com.example.wearzone.data.remote.mapper

import android.icu.number.Precision.currency
import android.util.Log
import com.example.data.BuildConfig
import com.example.wearzone.data.remote.dto.BillingDataDto
import com.example.wearzone.data.remote.dto.CustomerPaymentDto
import com.example.wearzone.data.remote.dto.IntentionItemDto
import com.example.wearzone.data.remote.dto.IntentionRequestDto
import com.example.wearzone.data.remote.dto.IntentionResponseDto
import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.PaymentIntention
import kotlin.math.roundToLong

private const val CURRENCY_MULTIPLIER = 100L

fun CheckoutData.toIntentionRequestDto(specialRef: String): IntentionRequestDto {

    val subtotal = cartItems.sumOf { it.price * it.quantity }

    val discount = subtotal - (totalAmount / 100.0)

    var remainingDiscount = discount

    val intentionItems = cartItems.mapIndexed { index, item ->

        val itemTotal = item.price * item.quantity

        val itemDiscount = if (index == cartItems.lastIndex) {
            remainingDiscount
        } else {
            val d = discount * (itemTotal / subtotal)
            remainingDiscount -= d
            d
        }

        val discountedUnitPrice = (itemTotal - itemDiscount) / item.quantity

        IntentionItemDto(
            name = item.title,
            amount = (discountedUnitPrice * CURRENCY_MULTIPLIER).roundToLong(),
            description = item.size?.let { "Size: $it" } ?: item.vendor,
            quantity = item.quantity)
    }.toMutableList()

    var itemsTotal = intentionItems.sumOf { it.amount * it.quantity }
    val difference = totalAmount - itemsTotal

    if (difference != 0L) {
        val lastIndex = intentionItems.lastIndex
        val last = intentionItems[lastIndex]

        intentionItems[lastIndex] = last.copy(
            amount = last.amount + (difference / last.quantity)
        )

        itemsTotal = intentionItems.sumOf { it.amount * it.quantity }
    }

    Log.d("PAYMOB", "Amount = $totalAmount")
    Log.d("PAYMOB", "Items Total = $itemsTotal")

    return IntentionRequestDto(
        amount = totalAmount,
        paymentMethods = listOf(BuildConfig.PAYMOB_INTEGRATION_ID.toLong()),
        items = intentionItems,
        billingData = BillingDataDto(
            firstName = customerInfo.firstName,
            lastName = customerInfo.lastName,
            phoneNumber = customerInfo.phone,
            email = customerInfo.email
        ),
        customer = CustomerPaymentDto(
            firstName = customerInfo.firstName,
            lastName = customerInfo.lastName,
            email = customerInfo.email
        ),
        specialReference = specialRef,
        extras = emptyMap(),
    )
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