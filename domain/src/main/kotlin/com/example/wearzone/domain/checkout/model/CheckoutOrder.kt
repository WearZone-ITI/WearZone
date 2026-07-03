package com.example.wearzone.domain.checkout.model

data class CheckoutOrderRequest(
    val customerId: Long,
    val lineItems: List<CheckoutLineItem>,
    val shippingAddress: CheckoutShippingAddress?,
    val discount: CheckoutDiscount? = null,
    val paymentMethod: CheckoutPaymentMethod = CheckoutPaymentMethod.CashOnDelivery,
)

data class CheckoutLineItem(
    val variantId: Long,
    val quantity: Int,
)

data class CheckoutShippingAddress(
    val firstName: String?,
    val lastName: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val province: String?,
    val country: String?,
    val zip: String?,
    val phone: String?,
)

data class CheckoutOrder(
    val id: Long,
    val name: String?,
)

class EmptyCartCheckoutException : Exception()
class InvalidCheckoutLineItemException : Exception()
class CheckoutOrderCreationException(cause: Throwable? = null) : Exception(cause)
class CheckoutCartClearException : Exception()
