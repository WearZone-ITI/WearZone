package com.example.wearzone.domain.checkout.usecase

import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.checkout.model.CheckoutCartClearException
import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.CheckoutLineItem
import com.example.wearzone.domain.checkout.model.CheckoutOrder
import com.example.wearzone.domain.checkout.model.CheckoutOrderRequest
import com.example.wearzone.domain.checkout.model.CheckoutPaymentMethod
import com.example.wearzone.domain.checkout.model.CheckoutShippingAddress
import com.example.wearzone.domain.checkout.model.CheckoutVariantValidationException
import com.example.wearzone.domain.checkout.model.EmptyCartCheckoutException
import com.example.wearzone.domain.checkout.model.InvalidCheckoutAddressException
import com.example.wearzone.domain.checkout.model.InvalidCheckoutLineItemException
import com.example.wearzone.domain.checkout.model.MissingCheckoutAddressException
import com.example.wearzone.domain.checkout.repository.ICheckoutRepository
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.product.repository.IProductRepository
import kotlinx.coroutines.flow.first

class PlaceOrderUseCase(
    private val checkoutRepository: ICheckoutRepository,
    private val cartRepository: ICartRepository,
    private val customerIdProvider: ICustomerIdProvider,
    private val customerAddressRepository: ICustomerAddressRepository,
    private val productRepository: IProductRepository,
) {
    suspend operator fun invoke(
        discount: CheckoutDiscount? = null,
        selectedAddressId: Long? = null,
        paymentMethod: CheckoutPaymentMethod = CheckoutPaymentMethod.CashOnDelivery,
        paymentId: String? = null,
    ): Result<CheckoutOrder> {
        val items = cartRepository.observeCart().first()
        if (items.isEmpty()) return Result.failure(EmptyCartCheckoutException())

        val customerId = customerIdProvider.getCurrentCustomerId().getOrElse {
            return Result.failure(it)
        }

        val lineItems = items.toValidatedCheckoutLineItems().getOrElse {
            return Result.failure(it)
        }
        val address = resolveShippingAddress(customerId, selectedAddressId)
            ?: return Result.failure(MissingCheckoutAddressException())
        val validAddress = address.normalizedForShopify().getOrElse {
            return Result.failure(it)
        }

        return checkoutRepository.createOrder(
            CheckoutOrderRequest(
                customerId = customerId,
                lineItems = lineItems,
                shippingAddress = validAddress,
                discount = discount,
                paymentMethod = paymentMethod,
                paymentId = paymentId,
            )
        ).onSuccess {
            when (cartRepository.clearCart()) {
                is DataResult.Success -> Unit
                is DataResult.Error -> return Result.failure(CheckoutCartClearException())
            }
        }
    }

    private suspend fun List<CartItem>.toValidatedCheckoutLineItems(): Result<List<CheckoutLineItem>> {
        val lineItems = mutableListOf<CheckoutLineItem>()

        for (item in this) {
            val variantId = item.variantId.toLongOrNull()
                ?: return Result.failure(
                    InvalidCheckoutLineItemException("Cart item \"${item.title}\" has an invalid Shopify variant_id."),
                )
            val productId = item.productId.toLongOrNull()
                ?: return Result.failure(
                    InvalidCheckoutLineItemException("Cart item \"${item.title}\" has an invalid Shopify product_id."),
                )
            if (item.quantity < MIN_ORDER_QUANTITY) {
                return Result.failure(
                    InvalidCheckoutLineItemException("Cart item \"${item.title}\" has an invalid quantity."),
                )
            }
            if (item.maxQuantity <= 0 || item.quantity > item.maxQuantity) {
                return Result.failure(
                    InvalidCheckoutLineItemException(
                        "Cart item \"${item.title}\" exceeds the available stock saved in the cart. Remove it and add it again.",
                    ),
                )
            }
            if (item.price <= MIN_PRICE) {
                return Result.failure(
                    InvalidCheckoutLineItemException("Cart item \"${item.title}\" has an invalid price."),
                )
            }

            val productDetail = productRepository.getProductDetail(productId).toResult().getOrElse { error ->
                return Result.failure(
                    CheckoutVariantValidationException(
                        "Could not refresh Shopify variant data for \"${item.title}\" before creating the order.",
                        error,
                    ),
                )
            }

            val currentVariant = productDetail.variants.firstOrNull { it.id == item.variantId }
                ?: return Result.failure(
                    CheckoutVariantValidationException(
                        "Variant ${item.variantId} for \"${productDetail.title}\" no longer exists in Shopify.",
                    ),
                )

            if (currentVariant.price <= MIN_PRICE) {
                return Result.failure(
                    CheckoutVariantValidationException(
                        "Variant ${item.variantId} for \"${productDetail.title}\" has an invalid Shopify price.",
                    ),
                )
            }
            if (currentVariant.availableQuantity <= 0) {
                return Result.failure(
                    CheckoutVariantValidationException(
                        "\"${productDetail.title}\" is now out of stock. Remove it from the cart and choose another item.",
                    ),
                )
            }
            if (item.quantity > currentVariant.availableQuantity) {
                return Result.failure(
                    CheckoutVariantValidationException(
                        "Only ${currentVariant.availableQuantity} item(s) are available for \"${productDetail.title}\". Update the cart quantity and try again.",
                    ),
                )
            }

            lineItems += CheckoutLineItem(
                variantId = variantId,
                quantity = item.quantity,
                price = currentVariant.price,
            )
        }

        return Result.success(lineItems)
    }

    private fun <T> DataResult<T>.toResult(): Result<T> = when (this) {
        is DataResult.Success -> Result.success(data)
        is DataResult.Error -> Result.failure(error.toThrowable())
    }

    private fun DomainError.toThrowable(): Throwable = when (this) {
        is DomainError.Network -> exception
        is DomainError.Unknown -> exception
        is DomainError.Server -> Exception(message ?: "Shopify server error $code")
    }

    private suspend fun resolveShippingAddress(
        customerId: Long,
        selectedAddressId: Long?,
    ): CheckoutShippingAddress? =
        customerAddressRepository.getAddresses(customerId)
            .getOrNull()
            ?.let { addresses ->
                when {
                    selectedAddressId != null ->
                        addresses.firstOrNull { it.id == selectedAddressId }
                    else ->
                        addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
                }
            }
            ?.toCheckoutShippingAddress()

    private fun CustomerAddress.toCheckoutShippingAddress(): CheckoutShippingAddress {
        val nameParts = name
            ?.split(Regex("\\s+"))
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val resolvedFirstName = firstName.nonBlankOrNull()
            ?: nameParts.firstOrNull()
            ?: DEFAULT_ADDRESS_FIRST_NAME
        val resolvedLastName = lastName.nonBlankOrNull()
            ?: nameParts.drop(1).joinToString(" ").nonBlankOrNull()
            ?: DEFAULT_ADDRESS_LAST_NAME

        return CheckoutShippingAddress(
            firstName = resolvedFirstName,
            lastName = resolvedLastName,
            address1 = address1,
            address2 = address2,
            city = city,
            province = province,
            country = countryName.nonBlankOrNull() ?: country.nonBlankOrNull() ?: countryCode.nonBlankOrNull(),
            zip = zip,
            phone = phone,
            provinceCode = provinceCode,
            countryCode = countryCode,
        )
    }

    private fun CheckoutShippingAddress.normalizedForShopify(): Result<CheckoutShippingAddress> {
        val missing = buildList {
            if (firstName.nonBlankOrNull() == null) add("first_name")
            if (lastName.nonBlankOrNull() == null) add("last_name")
            if (address1.nonBlankOrNull() == null) add("address1")
            if (city.nonBlankOrNull() == null) add("city")
            if (country.nonBlankOrNull() == null && countryCode.nonBlankOrNull() == null) add("country")
            if (phone.nonBlankOrNull() == null) add("phone")
        }

        if (missing.isNotEmpty()) {
            return Result.failure(
                InvalidCheckoutAddressException(
                    "Shipping address is missing: ${missing.joinToString()}. Update the address and try again.",
                ),
            )
        }

        return Result.success(
            copy(
                firstName = firstName.nonBlankOrNull(),
                lastName = lastName.nonBlankOrNull(),
                address1 = address1.nonBlankOrNull(),
                address2 = address2.nonBlankOrNull(),
                city = city.nonBlankOrNull(),
                province = province.nonBlankOrNull(),
                country = country.nonBlankOrNull(),
                zip = zip.nonBlankOrNull(),
                phone = phone.nonBlankOrNull(),
                provinceCode = provinceCode.nonBlankOrNull(),
                countryCode = countryCode.nonBlankOrNull(),
            ),
        )
    }

    private fun String?.nonBlankOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }

    private companion object {
        const val MIN_ORDER_QUANTITY = 1
        const val MIN_PRICE = 0.0
        const val DEFAULT_ADDRESS_FIRST_NAME = "Customer"
        const val DEFAULT_ADDRESS_LAST_NAME = "WearZone"
    }
}
