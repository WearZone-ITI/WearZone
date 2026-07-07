package com.example.wearzone.domain.checkout.model

data class CheckoutOrderRequest(
    val customerId: Long,
    val lineItems: List<CheckoutLineItem>,
    val shippingAddress: CheckoutShippingAddress?,
    val discount: CheckoutDiscount? = null,
    val paymentMethod: CheckoutPaymentMethod = CheckoutPaymentMethod.CashOnDelivery,
    val paymentId: String? = null,
)

data class CheckoutLineItem(
    val variantId: Long,
    val quantity: Int,
    val price: Double,
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
    val provinceCode: String? = null,
    val countryCode: String? = null,
)

data class CheckoutOrder(
    val id: Long,
    val name: String?,
)

class EmptyCartCheckoutException : Exception()
class InvalidCheckoutLineItemException(message: String? = null) : Exception(message)
class InvalidCheckoutAddressException(message: String? = null) : Exception(message)
class CheckoutVariantValidationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class CheckoutOrderCreationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class CheckoutCartClearException : Exception()
