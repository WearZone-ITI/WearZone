package com.example.domain.account.usecase

import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.account.usecase.CancelOrderUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelOrderUseCaseTest {

    @Test
    fun `invoke uses customer reason by default`() = runTest {
        val repository = FakeOrderHistoryRepository(Result.success(orderDetails()))
        val useCase = CancelOrderUseCase(repository)

        val result = useCase(ORDER_ID)

        assertEquals(ORDER_ID, repository.lastOrderId)
        assertEquals(OrderCancelReason.Customer, repository.lastReason)
        assertTrue(result.isSuccess)
    }

    private class FakeOrderHistoryRepository(
        private val cancelResult: Result<OrderDetails>,
    ) : IOrderHistoryRepository {
        var lastOrderId: Long? = null
            private set
        var lastReason: OrderCancelReason? = null
            private set

        override suspend fun getOrders(customerId: Long): Result<List<OrderHistory>> = Result.success(emptyList())

        override suspend fun getOrderDetails(orderId: Long, currentCustomerId: Long): Result<OrderDetails> =
            throw UnsupportedOperationException()

        override suspend fun cancelOrder(orderId: Long, reason: OrderCancelReason): Result<OrderDetails> {
            lastOrderId = orderId
            lastReason = reason
            return cancelResult
        }
    }

    private companion object {
        const val ORDER_ID = 99L

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

