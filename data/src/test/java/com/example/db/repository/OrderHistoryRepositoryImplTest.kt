package com.example.db.repository

import com.example.wearzone.data.remote.datasource.IOrderRemoteDataSource
import com.example.wearzone.data.remote.datasource.IProductRemoteDataSource
import com.example.wearzone.data.remote.dto.BrandDto
import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.OrderDto
import com.example.wearzone.data.remote.dto.OrderLineItemDto
import com.example.wearzone.data.remote.dto.ProductDto
import com.example.wearzone.data.remote.dto.ShopifyCancelOrderRequestDto
import com.example.wearzone.data.remote.dto.ShopifyProductDetail
import com.example.wearzone.data.repository.OrderHistoryRepositoryImpl
import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetailsOwnershipMismatchException
import com.example.wearzone.domain.account.model.OrderHistoryNetworkException
import com.example.wearzone.domain.account.model.OrderStatus
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderHistoryRepositoryImplTest {

    @Test
    fun `getOrders maps order dto to domain model`() = runTest {
        val remoteDataSource = FakeOrderRemoteDataSource(
            orders = listOf(orderDto()),
        )
        val productDataSource = FakeProductRemoteDataSource(
            products = listOf(productDto()),
        )
        val repository = OrderHistoryRepositoryImpl(remoteDataSource, productDataSource, Dispatchers.Unconfined)

        val result = repository.getOrders(CUSTOMER_ID)

        assertTrue(result.isSuccess)
        assertEquals(CUSTOMER_ID, remoteDataSource.lastCustomerId)
        assertEquals(listOf(PRODUCT_ID), productDataSource.lastProductIds)
        val order = result.getOrNull()?.first()
        assertEquals(99L, order?.id)
        assertEquals("#1001", order?.name)
        assertEquals(1001L, order?.orderNumber)
        assertEquals(485.0, order?.totalPrice ?: 0.0, 0.0)
        assertEquals("USD", order?.currencyCode)
        assertEquals(OrderStatus.Cancelled, order?.orderStatus)
        assertEquals("Silk Top", order?.lineItems?.first()?.title)
        assertEquals("https://example.com/top.png", order?.lineItems?.first()?.imageUrl)
        assertEquals(PRODUCT_ID, order?.lineItems?.first()?.productId)
    }

    @Test
    fun `getOrders maps io failure to order network exception`() = runTest {
        val repository = OrderHistoryRepositoryImpl(
            remoteDataSource = FakeOrderRemoteDataSource(error = IOException()),
            productRemoteDataSource = FakeProductRemoteDataSource(),
            ioDispatcher = Dispatchers.Unconfined,
        )

        val result = repository.getOrders(CUSTOMER_ID)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is OrderHistoryNetworkException)
    }

    @Test
    fun `getOrderDetails blocks order when customer id does not match`() = runTest {
        val repository = OrderHistoryRepositoryImpl(
            remoteDataSource = FakeOrderRemoteDataSource(
                order = orderDto(customerId = CUSTOMER_ID + 1),
            ),
            productRemoteDataSource = FakeProductRemoteDataSource(),
            ioDispatcher = Dispatchers.Unconfined,
        )

        val result = repository.getOrderDetails(99L, CUSTOMER_ID)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is OrderDetailsOwnershipMismatchException)
    }

    @Test
    fun `cancelOrder sends cancel reason and maps returned order`() = runTest {
        val remoteDataSource = FakeOrderRemoteDataSource(order = orderDto(customerId = CUSTOMER_ID))
        val repository = OrderHistoryRepositoryImpl(
            remoteDataSource = remoteDataSource,
            productRemoteDataSource = FakeProductRemoteDataSource(),
            ioDispatcher = Dispatchers.Unconfined,
        )

        val result = repository.cancelOrder(99L, OrderCancelReason.Customer)

        assertTrue(result.isSuccess)
        assertEquals("customer", remoteDataSource.lastCancelRequest?.reason)
        assertEquals(99L, result.getOrNull()?.id)
    }

    private class FakeOrderRemoteDataSource(
        private val orders: List<OrderDto> = emptyList(),
        private val order: OrderDto? = null,
        private val error: Throwable? = null,
    ) : IOrderRemoteDataSource {
        var lastCustomerId: Long? = null
            private set
        var lastCancelRequest: ShopifyCancelOrderRequestDto? = null
            private set

        override suspend fun getOrders(customerId: Long): List<OrderDto> {
            lastCustomerId = customerId
            error?.let { throw it }
            return orders
        }

        override suspend fun getOrder(orderId: Long): OrderDto {
            error?.let { throw it }
            return order ?: orders.first()
        }

        override suspend fun createOrder(request: com.example.wearzone.data.remote.dto.ShopifyOrderRequestDto): OrderDto =
            throw UnsupportedOperationException()

        override suspend fun cancelOrder(orderId: Long, request: ShopifyCancelOrderRequestDto): OrderDto {
            lastCancelRequest = request
            error?.let { throw it }
            return order ?: orders.first()
        }
    }

    private class FakeProductRemoteDataSource(
        private val products: List<ProductDto> = emptyList(),
    ) : IProductRemoteDataSource {
        var lastProductIds: List<Long> = emptyList()
            private set

        override suspend fun getCategories(): List<CategoryDto> = emptyList()

        override suspend fun getBrands(): List<BrandDto> = emptyList()

        override suspend fun getProducts(vendor: String?): List<ProductDto> = products

        override suspend fun getProducts(collectionId: Long?): List<ProductDto> = products

        override suspend fun getProductsByIds(productIds: List<Long>): List<ProductDto> {
            lastProductIds = productIds
            return products
        }

        override suspend fun getProductDetail(productId: Long): ShopifyProductDetail =
            throw UnsupportedOperationException()
    }

    private companion object {
        const val CUSTOMER_ID = 9307871641828L
        const val PRODUCT_ID = 9091143729380L

        fun orderDto(customerId: Long = CUSTOMER_ID): OrderDto =
            OrderDto(
                id = 99L,
                name = "#1001",
                orderNumber = 1001L,
                createdAt = "2026-07-01T10:00:00Z",
                totalPrice = "485.00",
                currency = "USD",
                customer = com.example.wearzone.data.remote.dto.OrderCustomerDto(id = customerId),
                financialStatus = "paid",
                fulfillmentStatus = "fulfilled",
                cancelledAt = "2026-07-02T10:00:00Z",
                lineItems = listOf(
                    OrderLineItemDto(
                        id = 1L,
                        productId = PRODUCT_ID,
                        title = "Silk Top",
                        quantity = 1,
                    )
                ),
            )

        fun productDto(): ProductDto =
            ProductDto(
                id = PRODUCT_ID.toString(),
                variantId = "456",
                title = "Silk Top",
                vendor = "WearZone",
                price = 485.0,
                currencyCode = "USD",
                imageUrl = "https://example.com/top.png",
            )
    }
}
