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
import com.example.wearzone.domain.checkout.model.EmptyCartCheckoutException
import com.example.wearzone.domain.checkout.model.InvalidCheckoutLineItemException
import com.example.wearzone.domain.checkout.model.MissingCheckoutAddressException
import com.example.wearzone.domain.checkout.repository.ICheckoutRepository
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import kotlinx.coroutines.flow.first

class PlaceOrderUseCase(
    private val checkoutRepository: ICheckoutRepository,
    private val cartRepository: ICartRepository,
    private val customerIdProvider: ICustomerIdProvider,
    private val customerAddressRepository: ICustomerAddressRepository,
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

        val lineItems = items.toCheckoutLineItems().getOrElse {
            return Result.failure(it)
        }
        val address = resolveShippingAddress(customerId, selectedAddressId)
            ?: return Result.failure(MissingCheckoutAddressException())

        return checkoutRepository.createOrder(
            CheckoutOrderRequest(
                customerId = customerId,
                lineItems = lineItems,
                shippingAddress = address,
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

    private fun List<CartItem>.toCheckoutLineItems(): Result<List<CheckoutLineItem>> {
        val lineItems = map { item ->
            val variantId = item.variantId.toLongOrNull()
                ?: return Result.failure(InvalidCheckoutLineItemException())
            CheckoutLineItem(
                variantId = variantId,
                quantity = item.quantity,
            )
        }
        return Result.success(lineItems)
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

    private fun CustomerAddress.toCheckoutShippingAddress(): CheckoutShippingAddress =
        CheckoutShippingAddress(
            firstName = firstName,
            lastName = lastName,
            address1 = address1,
            address2 = address2,
            city = city,
            province = province,
            country = country,
            zip = zip,
            phone = phone,
        )
}
