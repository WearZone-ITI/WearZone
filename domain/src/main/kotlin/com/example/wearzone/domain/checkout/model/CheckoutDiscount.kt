package com.example.wearzone.domain.checkout.model

data class CheckoutDiscount(
    val code: String,
    val value: Double,
    val valueType: CheckoutDiscountValueType,
    val calculatedAmount: Double,
)

enum class CheckoutDiscountValueType {
    Percentage,
    FixedAmount,
}

class EmptyDiscountCodeException : Exception()
class InvalidDiscountCodeException : Exception()
class DiscountLookupException(cause: Throwable? = null) : Exception(cause)
