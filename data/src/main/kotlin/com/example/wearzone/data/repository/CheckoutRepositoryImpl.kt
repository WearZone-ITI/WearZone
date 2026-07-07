package com.example.wearzone.data.repository

import android.util.Log
import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.datasource.IOrderRemoteDataSource
import com.example.wearzone.data.remote.dto.OrderDto
import com.example.wearzone.data.remote.dto.ShopifyOrderCustomerDto
import com.example.wearzone.data.remote.dto.ShopifyOrderDiscountCodeDto
import com.example.wearzone.data.remote.dto.ShopifyOrderLineItemRequestDto
import com.example.wearzone.data.remote.dto.ShopifyOrderPayloadDto
import com.example.wearzone.data.remote.dto.ShopifyOrderRequestDto
import com.example.wearzone.data.remote.dto.ShopifyOrderShippingAddressDto
import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.CheckoutDiscountValueType
import com.example.wearzone.domain.checkout.model.CheckoutOrder
import com.example.wearzone.domain.checkout.model.CheckoutOrderCreationException
import com.example.wearzone.domain.checkout.model.CheckoutOrderRequest
import com.example.wearzone.domain.checkout.model.CheckoutPaymentMethod
import com.example.wearzone.domain.checkout.model.CheckoutShippingAddress
import com.example.wearzone.domain.checkout.repository.ICheckoutRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class CheckoutRepositoryImpl @Inject constructor(
    private val remoteDataSource: IOrderRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICheckoutRepository {

    override suspend fun createOrder(request: CheckoutOrderRequest): Result<CheckoutOrder> =
        withContext(ioDispatcher) {
            val dto = request.toDto()
            logOrderCreateAttempt(request)

            runCatchingCancellable {
                remoteDataSource.createOrder(dto).toDomain()
            }.fold(
                onSuccess = { order ->
                    Result.success(order)
                },
                onFailure = { error ->
                    val mappedError = error.toOrderCreationException()
                    Result.failure(mappedError)
                },
            )
        }

    private fun CheckoutOrderRequest.toDto(): ShopifyOrderRequestDto {
        val addressDto = shippingAddress?.toDto()

        return ShopifyOrderRequestDto(
            order = ShopifyOrderPayloadDto(
                lineItems = lineItems.map {
                    ShopifyOrderLineItemRequestDto(
                        variantId = it.variantId,
                        quantity = it.quantity,
                        price = it.price.toOrderPriceString(),
                    )
                },
                customer = ShopifyOrderCustomerDto(id = customerId),
                shippingAddress = addressDto,
                billingAddress = addressDto,
                discountCodes = discount?.let { appliedDiscount ->
                    listOf(appliedDiscount.toOrderDiscountDto())
                },
                note = paymentId?.let { "Payment ID: $it\n" }.orEmpty() + paymentMethod.toOrderNote(),
                financialStatus = if (paymentMethod == CheckoutPaymentMethod.CreditCard) "paid" else "pending",
            )
        )
    }

    private fun CheckoutDiscount.toOrderDiscountDto(): ShopifyOrderDiscountCodeDto =
        when (valueType) {
            CheckoutDiscountValueType.Percentage -> ShopifyOrderDiscountCodeDto(
                code = code,
                amount = calculatedAmount.toDiscountAmountString(),
                type = DISCOUNT_TYPE_PERCENTAGE,
            )
            CheckoutDiscountValueType.FixedAmount -> ShopifyOrderDiscountCodeDto(
                code = code,
                amount = calculatedAmount.toDiscountAmountString(),
                type = DISCOUNT_TYPE_FIXED_AMOUNT,
            )
        }

    private fun CheckoutPaymentMethod.toOrderNote(): String =
        when (this) {
            CheckoutPaymentMethod.CashOnDelivery -> ORDER_NOTE_CASH_ON_DELIVERY
            CheckoutPaymentMethod.CreditCard -> ORDER_NOTE_CREDIT_CARD
        }

    private fun Double.toDiscountAmountString(): String =
        String.format(Locale.US, "%.2f", this)


    private fun Double.toOrderPriceString(): String =
        String.format(Locale.US, "%.2f", this)

    private fun CheckoutShippingAddress.toDto(): ShopifyOrderShippingAddressDto =
        ShopifyOrderShippingAddressDto(
            firstName = firstName,
            lastName = lastName,
            address1 = address1,
            address2 = address2,
            city = city,
            province = province,
            country = country,
            zip = zip,
            phone = phone,
            provinceCode = provinceCode,
            countryCode = countryCode,
        )

    private fun OrderDto.toDomain(): CheckoutOrder =
        CheckoutOrder(
            id = id,
            name = name,
        )

    private fun Throwable.toOrderCreationException(): CheckoutOrderCreationException =
        when (this) {
            is CheckoutOrderCreationException -> this
            is HttpException -> {
                val body = safeErrorBody()
                CheckoutOrderCreationException(
                    message = buildString {
                        append("Shopify order create failed: HTTP ${code()}")
                        if (body.isNotBlank()) append(" - ${body.compactForMessage()}")
                    },
                    cause = this,
                )
            }
            is IOException -> CheckoutOrderCreationException(
                message = "Shopify order create failed: network error. Check your connection and Shopify Admin API access.",
                cause = this,
            )
            else -> CheckoutOrderCreationException(
                message = message?.takeIf { it.isNotBlank() }
                    ?: "Shopify order create failed before a valid response was returned.",
                cause = this,
            )
        }

    private fun HttpException.safeErrorBody(): String =
        try {
            response()?.errorBody()?.string().orEmpty()
        } catch (_: Exception) {
            ""
        }

    private fun String.compactForMessage(): String =
        replace(Regex("\\s+"), " ").trim().take(MAX_USER_ERROR_BODY_CHARS)

    private fun logOrderCreateAttempt(request: CheckoutOrderRequest) {
        Log.d(
            TAG,
            buildString {
                append("Creating Shopify order: customer_id=${request.customerId}, ")
                append("line_items=")
                append(request.lineItems.joinToString(prefix = "[", postfix = "]") {
                    "{variant_id=${it.variantId}, quantity=${it.quantity}, price=${it.price.toOrderPriceString()}}"
                })
                append(", shipping_address_valid=${request.shippingAddress != null}")
                request.shippingAddress?.let { address ->
                    append(", city=${address.city.orEmpty()}, country=${address.country.orEmpty()}, country_code=${address.countryCode.orEmpty()}")
                }
            },
        )
    }

    private companion object {
        const val TAG = "ShopifyOrderCreate"
        const val DISCOUNT_TYPE_PERCENTAGE = "percentage"
        const val MAX_USER_ERROR_BODY_CHARS = 500
        const val DISCOUNT_TYPE_FIXED_AMOUNT = "fixed_amount"
        const val ORDER_NOTE_CASH_ON_DELIVERY = "Payment method: Cash on Delivery"
        const val ORDER_NOTE_CREDIT_CARD = "Payment method: Credit Card (Paymob)"
    }
}
