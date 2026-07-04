package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.datasource.IOrderRemoteDataSource
import com.example.wearzone.data.remote.datasource.IProductRemoteDataSource
import com.example.wearzone.data.remote.dto.OrderDto
import com.example.wearzone.data.remote.dto.OrderLineItemDto
import com.example.wearzone.data.remote.dto.OrderAddressDto
import com.example.wearzone.data.remote.dto.ShopifyCancelOrderRequestDto
import com.example.wearzone.domain.account.model.OrderCancelNotAllowedException
import com.example.wearzone.domain.account.model.OrderCancelFailedException
import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.model.OrderDetailsAddress
import com.example.wearzone.domain.account.model.OrderDetailsLineItem
import com.example.wearzone.domain.account.model.OrderDetailsNotFoundException
import com.example.wearzone.domain.account.model.OrderDetailsOwnershipMismatchException
import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.model.OrderHistoryCustomerNotFoundException
import com.example.wearzone.domain.account.model.OrderHistoryLineItem
import com.example.wearzone.domain.account.model.OrderHistoryNetworkException
import com.example.wearzone.domain.account.model.OrderHistoryPermissionException
import com.example.wearzone.domain.account.model.OrderHistoryResponseParseException
import com.example.wearzone.domain.account.model.OrderHistoryUnknownException
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import javax.inject.Inject

class OrderHistoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: IOrderRemoteDataSource,
    private val productRemoteDataSource: IProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IOrderHistoryRepository {

    override suspend fun getOrders(customerId: Long): Result<List<OrderHistory>> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val orders = remoteDataSource.getOrders(customerId)
                val productImagesById = loadProductImagesById(orders)
                orders.map { it.toDomain(productImagesById) }
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toOrderException()) },
            )
        }

    override suspend fun getOrderDetails(orderId: Long, currentCustomerId: Long): Result<OrderDetails> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val order = remoteDataSource.getOrder(orderId)
                if (order.customer?.id != currentCustomerId) {
                    throw OrderDetailsOwnershipMismatchException()
                }
                val productImagesById = loadProductImagesById(listOf(order))
                order.toDetailsDomain(productImagesById)
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toOrderDetailsException()) },
            )
        }

    override suspend fun cancelOrder(orderId: Long, reason: OrderCancelReason): Result<OrderDetails> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val order = remoteDataSource.cancelOrder(
                    orderId = orderId,
                    request = ShopifyCancelOrderRequestDto(reason = reason.value),
                )
                val productImagesById = loadProductImagesById(listOf(order))
                order.toDetailsDomain(productImagesById)
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toOrderCancelException()) },
            )
        }

    private suspend fun loadProductImagesById(orders: List<OrderDto>): Map<Long, String> {
        val productIds = orders
            .flatMap { order -> order.lineItems.mapNotNull { it.productId } }
            .distinct()
        if (productIds.isEmpty()) return emptyMap()
        return runCatchingCancellable {
            productRemoteDataSource.getProductsByIds(productIds)
                .mapNotNull { product ->
                    val productId = product.id.toLongOrNull()
                    val imageUrl = product.imageUrl?.takeIf { it.isNotBlank() }
                    if (productId != null && imageUrl != null) productId to imageUrl else null
                }
                .toMap()
        }.getOrDefault(emptyMap())
    }

    private fun Throwable.toOrderException(): Throwable =
        when (this) {
            is HttpException -> toOrderHttpException()
            is IOException -> OrderHistoryNetworkException()
            is SerializationException -> OrderHistoryResponseParseException()
            else -> OrderHistoryUnknownException(this)
        }

    private fun Throwable.toOrderDetailsException(): Throwable =
        when (this) {
            is OrderDetailsOwnershipMismatchException -> this
            is HttpException -> toOrderDetailsHttpException()
            is IOException -> OrderHistoryNetworkException()
            is SerializationException -> OrderHistoryResponseParseException()
            else -> OrderHistoryUnknownException(this)
        }

    private fun Throwable.toOrderCancelException(): Throwable =
        when (this) {
            is HttpException -> toOrderCancelHttpException()
            is IOException -> OrderHistoryNetworkException()
            is SerializationException -> OrderHistoryResponseParseException()
            else -> OrderCancelFailedException(this)
        }

    private fun HttpException.toOrderHttpException(): Throwable =
        when (code()) {
            401, 403 -> OrderHistoryPermissionException()
            404 -> OrderHistoryCustomerNotFoundException()
            408, 429, in 500..599 -> OrderHistoryNetworkException()
            else -> OrderHistoryUnknownException(this)
        }

    private fun HttpException.toOrderDetailsHttpException(): Throwable =
        when (code()) {
            401, 403 -> OrderHistoryPermissionException()
            404 -> OrderDetailsNotFoundException()
            408, 429, in 500..599 -> OrderHistoryNetworkException()
            else -> OrderHistoryUnknownException(this)
        }

    private fun HttpException.toOrderCancelHttpException(): Throwable =
        when (code()) {
            401, 403 -> OrderHistoryPermissionException()
            404 -> OrderDetailsNotFoundException()
            408, 429, in 500..599 -> OrderHistoryNetworkException()
            422 -> OrderCancelNotAllowedException()
            else -> OrderCancelFailedException(this)
        }

    private fun OrderDto.toDomain(productImagesById: Map<Long, String>): OrderHistory =
        OrderHistory(
            id = id,
            name = name,
            orderNumber = orderNumber,
            createdAt = createdAt,
            totalPrice = totalPrice?.toDoubleOrNull() ?: 0.0,
            currencyCode = currency.orEmpty(),
            financialStatus = financialStatus,
            fulfillmentStatus = fulfillmentStatus,
            orderStatus = when {
                !cancelledAt.isNullOrBlank() -> OrderStatus.Cancelled
                !closedAt.isNullOrBlank() -> OrderStatus.Closed
                else -> OrderStatus.Open
            },
            lineItems = lineItems.map { it.toDomain(productImagesById) },
            trackingNumber = fulfillments.firstNotNullOfOrNull {
                it.trackingNumber?.takeIf { trackingNumber -> trackingNumber.isNotBlank() }
            },
            trackingUrl = fulfillments.firstNotNullOfOrNull { fulfillment ->
                fulfillment.trackingUrl?.takeIf { it.isNotBlank() }
                    ?: fulfillment.trackingUrls.firstOrNull { it.isNotBlank() }
            },
        )

    private fun OrderDto.toDetailsDomain(productImagesById: Map<Long, String>): OrderDetails =
        OrderDetails(
            id = id,
            name = name,
            orderNumber = orderNumber,
            createdAt = createdAt,
            cancelledAt = cancelledAt,
            closedAt = closedAt,
            cancelReason = cancelReason,
            totalPrice = totalPrice?.toDoubleOrNull() ?: 0.0,
            subtotalPrice = currentSubtotalPrice?.toDoubleOrNull()
                ?: subtotalPrice?.toDoubleOrNull()
                ?: 0.0,
            shippingPrice = totalShippingPriceSet?.shopMoney?.amount?.toDoubleOrNull() ?: 0.0,
            taxPrice = currentTotalTax?.toDoubleOrNull()
                ?: totalTax?.toDoubleOrNull()
                ?: 0.0,
            currencyCode = currency.orEmpty(),
            financialStatus = financialStatus,
            fulfillmentStatus = fulfillmentStatus,
            orderStatus = toOrderStatus(),
            canCancel = canCancel(),
            lineItems = lineItems.map { it.toDetailsDomain(productImagesById) },
            shippingAddress = shippingAddress?.toDomain(),
            paymentMethods = paymentGatewayNames.filter { it.isNotBlank() },
            trackingNumber = fulfillments.firstNotNullOfOrNull {
                it.trackingNumber?.takeIf { trackingNumber -> trackingNumber.isNotBlank() }
            },
            trackingUrl = fulfillments.firstNotNullOfOrNull { fulfillment ->
                fulfillment.trackingUrl?.takeIf { it.isNotBlank() }
                    ?: fulfillment.trackingUrls.firstOrNull { it.isNotBlank() }
            },
        )

    private fun OrderDto.toOrderStatus(): OrderStatus =
        when {
            !cancelledAt.isNullOrBlank() -> OrderStatus.Cancelled
            !closedAt.isNullOrBlank() -> OrderStatus.Closed
            else -> OrderStatus.Open
        }

    private fun OrderDto.canCancel(): Boolean {
        if (toOrderStatus() != OrderStatus.Open) return false
        val fulfillment = fulfillmentStatus.orEmpty().lowercase()
        val isFulfilledOrShipped = fulfillment == "fulfilled" ||
            fulfillment == "delivered" ||
            fulfillment == "shipped" ||
            fulfillments.any { fulfillmentDto ->
                val shipmentStatus = fulfillmentDto.shipmentStatus.orEmpty().lowercase()
                shipmentStatus == "delivered" || shipmentStatus == "shipped"
            }
        if (isFulfilledOrShipped) return false

        val isPaid = financialStatus.orEmpty().equals("paid", ignoreCase = true)
        return !(isPaid && fulfillments.isNotEmpty())
    }

    private fun OrderLineItemDto.toDomain(productImagesById: Map<Long, String>): OrderHistoryLineItem =
        OrderHistoryLineItem(
            id = id,
            productId = productId,
            variantId = variantId,
            title = title?.takeIf { it.isNotBlank() }
                ?: name?.takeIf { it.isNotBlank() }
                ?: "",
            quantity = quantity,
            price = price?.toDoubleOrNull() ?: 0.0,
            imageUrl = image?.src?.takeIf { it.isNotBlank() }
                ?: productId?.let(productImagesById::get),
        )

    private fun OrderLineItemDto.toDetailsDomain(productImagesById: Map<Long, String>): OrderDetailsLineItem =
        OrderDetailsLineItem(
            id = id,
            productId = productId,
            variantId = variantId,
            title = title?.takeIf { it.isNotBlank() }
                ?: name?.takeIf { it.isNotBlank() }
                ?: "",
            variantTitle = variantTitle?.takeIf { it.isNotBlank() },
            quantity = quantity,
            price = price?.toDoubleOrNull() ?: 0.0,
            imageUrl = image?.src?.takeIf { it.isNotBlank() }
                ?: productId?.let(productImagesById::get),
        )

    private fun OrderAddressDto.toDomain(): OrderDetailsAddress =
        OrderDetailsAddress(
            name = name,
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
