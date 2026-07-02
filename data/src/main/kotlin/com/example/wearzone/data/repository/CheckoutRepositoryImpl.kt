package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.datasource.IOrderRemoteDataSource
import com.example.wearzone.data.remote.dto.OrderDto
import com.example.wearzone.data.remote.dto.ShopifyOrderCustomerDto
import com.example.wearzone.data.remote.dto.ShopifyOrderLineItemRequestDto
import com.example.wearzone.data.remote.dto.ShopifyOrderPayloadDto
import com.example.wearzone.data.remote.dto.ShopifyOrderRequestDto
import com.example.wearzone.data.remote.dto.ShopifyOrderShippingAddressDto
import com.example.wearzone.domain.checkout.model.CheckoutOrder
import com.example.wearzone.domain.checkout.model.CheckoutOrderCreationException
import com.example.wearzone.domain.checkout.model.CheckoutOrderRequest
import com.example.wearzone.domain.checkout.model.CheckoutShippingAddress
import com.example.wearzone.domain.checkout.repository.ICheckoutRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckoutRepositoryImpl @Inject constructor(
    private val remoteDataSource: IOrderRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICheckoutRepository {

    override suspend fun createOrder(request: CheckoutOrderRequest): Result<CheckoutOrder> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                remoteDataSource.createOrder(request.toDto()).toDomain()
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(CheckoutOrderCreationException(it)) },
            )
        }

    private fun CheckoutOrderRequest.toDto(): ShopifyOrderRequestDto =
        ShopifyOrderRequestDto(
            order = ShopifyOrderPayloadDto(
                lineItems = lineItems.map {
                    ShopifyOrderLineItemRequestDto(
                        variantId = it.variantId,
                        quantity = it.quantity,
                    )
                },
                customer = ShopifyOrderCustomerDto(id = customerId),
                shippingAddress = shippingAddress?.toDto(),
            )
        )

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
        )

    private fun OrderDto.toDomain(): CheckoutOrder =
        CheckoutOrder(
            id = id,
            name = name,
        )
}
