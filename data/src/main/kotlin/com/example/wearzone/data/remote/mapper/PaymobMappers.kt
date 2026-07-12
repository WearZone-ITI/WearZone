package com.example.wearzone.data.remote.mapper

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

    val subtotalMinor = cartItems.sumOf {
        (it.price * CURRENCY_MULTIPLIER).roundToLong() * it.quantity
    }

    val discountMinor = subtotalMinor - totalAmount
    var remainingDiscount = discountMinor

    val intentionItems = mutableListOf<IntentionItemDto>()

    cartItems.forEachIndexed { index, item ->

        val itemTotalMinor =
            (item.price * CURRENCY_MULTIPLIER).roundToLong() * item.quantity

        val itemDiscountMinor =
            if (index == cartItems.lastIndex) {
                remainingDiscount
            } else {
                val d = (discountMinor.toDouble() * itemTotalMinor / subtotalMinor)
                    .roundToLong()

                remainingDiscount -= d
                d
            }

        val finalItemTotalMinor = itemTotalMinor - itemDiscountMinor

        val baseUnitPrice = finalItemTotalMinor / item.quantity
        var remainder = finalItemTotalMinor % item.quantity

        repeat(item.quantity) {
            val amount = if (remainder > 0) {
                remainder--
                baseUnitPrice + 1
            } else {
                baseUnitPrice
            }

            intentionItems.add(
                IntentionItemDto(
                    name = item.title,
                    amount = amount,
                    quantity = 1,
                    description = item.size?.let { "Size: $it" } ?: item.vendor
                )
            )
        }
    }

    val itemsTotal = intentionItems.sumOf { it.amount }

    check(itemsTotal == totalAmount) {
        "Items total ($itemsTotal) != total amount ($totalAmount)"
    }

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
        extras = emptyMap()
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