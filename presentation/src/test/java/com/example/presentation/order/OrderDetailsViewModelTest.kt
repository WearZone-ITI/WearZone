package com.example.presentation.order

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.presentation.R
import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.account.usecase.CancelOrderUseCase
import com.example.wearzone.domain.account.usecase.GetOrderDetailsUseCase
import com.example.wearzone.domain.account.usecase.GetOrderHistoryUseCase
import com.example.wearzone.domain.account.usecase.OrderHistoryUseCases
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase
import com.example.wearzone.presentation.order.details.OrderDetailsUiEffect
import com.example.wearzone.presentation.order.details.OrderDetailsUiIntent
import com.example.wearzone.presentation.order.details.OrderDetailsUiState
import com.example.wearzone.presentation.order.details.OrderDetailsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loading order resolves current customer id and becomes content`() = runTest {
        val repository = FakeOrderHistoryRepository(detailsResult = Result.success(orderDetails(canCancel = true)))
        val viewModel = OrderDetailsViewModel(createUseCases(repository))

        viewModel.handleIntent(OrderDetailsUiIntent.LoadOrder(ORDER_ID))
        advanceUntilIdle()

        assertEquals(CUSTOMER_ID, repository.lastDetailsCustomerId)
        val state = viewModel.uiState.value as OrderDetailsUiState.Content
        assertEquals("#1001", state.order.displayName)
        assertTrue(state.order.canCancel)
    }

    @Test
    fun `cancel click emits confirmation dialog effect`() = runTest {
        val repository = FakeOrderHistoryRepository(detailsResult = Result.success(orderDetails(canCancel = true)))
        val viewModel = OrderDetailsViewModel(createUseCases(repository))
        viewModel.handleIntent(OrderDetailsUiIntent.LoadOrder(ORDER_ID))
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.handleIntent(OrderDetailsUiIntent.OnCancelOrderClicked)

            assertEquals(OrderDetailsUiEffect.ShowCancelOrderDialog, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmed cancel calls repository with customer reason and shows success`() = runTest {
        val repository = FakeOrderHistoryRepository(
            detailsResult = Result.success(orderDetails(canCancel = true)),
            cancelResult = Result.success(orderDetails(canCancel = false, status = OrderStatus.Cancelled)),
        )
        val viewModel = OrderDetailsViewModel(createUseCases(repository))
        viewModel.handleIntent(OrderDetailsUiIntent.LoadOrder(ORDER_ID))
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.handleIntent(OrderDetailsUiIntent.OnCancelConfirmed)
            advanceUntilIdle()

            val effect = awaitItem() as OrderDetailsUiEffect.ShowMessage
            assertEquals(R.string.order_details_cancel_success, effect.messageRes)
            assertEquals(OrderCancelReason.Customer, repository.lastCancelReason)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createUseCases(
        repository: FakeOrderHistoryRepository,
        provider: ICustomerIdProvider = FakeCustomerIdProvider(Result.success(CUSTOMER_ID)),
    ): OrderHistoryUseCases =
        OrderHistoryUseCases(
            getCurrentCustomerId = GetCurrentCustomerIdUseCase(provider),
            getOrderHistory = GetOrderHistoryUseCase(repository),
            getOrderDetails = GetOrderDetailsUseCase(repository),
            cancelOrder = CancelOrderUseCase(repository),
        )

    private class FakeCustomerIdProvider(
        private val result: Result<Long>,
    ) : ICustomerIdProvider {
        override suspend fun getCurrentCustomerId(): Result<Long> = result
    }

    private class FakeOrderHistoryRepository(
        private val detailsResult: Result<OrderDetails>,
        private val cancelResult: Result<OrderDetails> = detailsResult,
    ) : IOrderHistoryRepository {
        var lastDetailsCustomerId: Long? = null
            private set
        var lastCancelReason: OrderCancelReason? = null
            private set

        override suspend fun getOrders(customerId: Long) = Result.success(emptyList<com.example.wearzone.domain.account.model.OrderHistory>())

        override suspend fun getOrderDetails(orderId: Long, currentCustomerId: Long): Result<OrderDetails> {
            lastDetailsCustomerId = currentCustomerId
            return detailsResult
        }

        override suspend fun cancelOrder(orderId: Long, reason: OrderCancelReason): Result<OrderDetails> {
            lastCancelReason = reason
            return cancelResult
        }
    }

    private companion object {
        const val ORDER_ID = 99L
        const val CUSTOMER_ID = 9307871641828L

        fun orderDetails(canCancel: Boolean, status: OrderStatus = OrderStatus.Open): OrderDetails =
            OrderDetails(
                id = ORDER_ID,
                name = "#1001",
                orderNumber = 1001L,
                createdAt = "2026-07-01T10:00:00Z",
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
                orderStatus = status,
                canCancel = canCancel,
                lineItems = emptyList(),
                shippingAddress = null,
                paymentMethods = emptyList(),
                trackingNumber = null,
                trackingUrl = null,
            )
    }
}

