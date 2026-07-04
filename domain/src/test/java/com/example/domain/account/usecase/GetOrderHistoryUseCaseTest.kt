package com.example.domain.account.usecase

import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.account.usecase.GetOrderHistoryUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetOrderHistoryUseCaseTest {

    @Test
    fun `invoke forwards customer id and returns repository orders`() = runTest {
        val repository = FakeOrderHistoryRepository(
            result = Result.success(listOf(order())),
        )
        val useCase = GetOrderHistoryUseCase(repository)

        val result = useCase(CUSTOMER_ID)

        assertEquals(CUSTOMER_ID, repository.lastCustomerId)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    private class FakeOrderHistoryRepository(
        private val result: Result<List<OrderHistory>>,
    ) : IOrderHistoryRepository {
        var lastCustomerId: Long? = null
            private set

        override suspend fun getOrders(customerId: Long): Result<List<OrderHistory>> {
            lastCustomerId = customerId
            return result
        }

        override suspend fun getOrderDetails(orderId: Long, currentCustomerId: Long): Result<OrderDetails> =
            throw UnsupportedOperationException()

        override suspend fun cancelOrder(orderId: Long, reason: OrderCancelReason): Result<OrderDetails> =
            throw UnsupportedOperationException()
    }

    private companion object {
        const val CUSTOMER_ID = 9307871641828L

        fun order(): OrderHistory =
            OrderHistory(
                id = 1L,
                name = "#1001",
                orderNumber = 1001L,
                createdAt = "2026-07-01T10:00:00Z",
                totalPrice = 120.0,
                currencyCode = "USD",
                financialStatus = "paid",
                fulfillmentStatus = "fulfilled",
                orderStatus = OrderStatus.Open,
                lineItems = emptyList(),
                trackingNumber = null,
                trackingUrl = null,
            )
    }
}
