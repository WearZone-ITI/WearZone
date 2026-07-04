package com.example.domain.account.usecase

import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.account.usecase.GetOrderDetailsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetOrderDetailsUseCaseTest {

    @Test
    fun `invoke forwards order id and customer id and returns details`() = runTest {
        val repository = FakeOrderHistoryRepository(Result.success(orderDetails()))
        val useCase = GetOrderDetailsUseCase(repository)

        val result = useCase(ORDER_ID, CUSTOMER_ID)

        assertEquals(ORDER_ID, repository.lastOrderId)
        assertEquals(CUSTOMER_ID, repository.lastCustomerId)
        assertTrue(result.isSuccess)
        assertEquals(ORDER_ID, result.getOrNull()?.id)
    }

    private class FakeOrderHistoryRepository(
        private val detailsResult: Result<OrderDetails>,
    ) : IOrderHistoryRepository {
        var lastOrderId: Long? = null
            private set
        var lastCustomerId: Long? = null
            private set

        override suspend fun getOrders(customerId: Long) = Result.success(emptyList<com.example.wearzone.domain.account.model.OrderHistory>())

        override suspend fun getOrderDetails(orderId: Long, currentCustomerId: Long): Result<OrderDetails> {
            lastOrderId = orderId
            lastCustomerId = currentCustomerId
            return detailsResult
        }

        override suspend fun cancelOrder(orderId: Long, reason: OrderCancelReason): Result<OrderDetails> =
            throw UnsupportedOperationException()
    }

    private companion object {
        const val ORDER_ID = 99L
        const val CUSTOMER_ID = 9307871641828L

        fun orderDetails(): OrderDetails =
            OrderDetails(
                id = ORDER_ID,
                name = "#1001",
                orderNumber = 1001L,
                createdAt = null,
                cancelledAt = null,
                closedAt = null,
                cancelReason = null,
                totalPrice = 120.0,
                subtotalPrice = 100.0,
                shippingPrice = 10.0,
                taxPrice = 10.0,
                currencyCode = "USD",
                financialStatus = "paid",
                fulfillmentStatus = null,
                orderStatus = OrderStatus.Open,
                canCancel = true,
                lineItems = emptyList(),
                shippingAddress = null,
                paymentMethods = emptyList(),
                trackingNumber = null,
                trackingUrl = null,
            )
    }
}

