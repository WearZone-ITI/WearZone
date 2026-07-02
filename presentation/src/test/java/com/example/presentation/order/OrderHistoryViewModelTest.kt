package com.example.presentation.order

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.presentation.R
import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.model.OrderHistoryLineItem
import com.example.wearzone.domain.account.model.OrderHistoryNetworkException
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.account.usecase.GetOrderHistoryUseCase
import com.example.wearzone.domain.account.usecase.OrderHistoryUseCases
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase
import com.example.wearzone.presentation.order.history.OrderHistoryUiEffect
import com.example.wearzone.presentation.order.history.OrderHistoryUiIntent
import com.example.wearzone.presentation.order.history.OrderHistoryUiState
import com.example.wearzone.presentation.order.history.OrderHistoryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial success becomes content and uses resolved Shopify customer id`() = runTest {
        val repository = FakeOrderHistoryRepository(
            result = Result.success(listOf(order())),
        )
        val viewModel = OrderHistoryViewModel(createUseCases(repository))
        advanceUntilIdle()

        assertEquals(CUSTOMER_ID, repository.lastCustomerId)
        val state = viewModel.uiState.value as OrderHistoryUiState.Content
        assertEquals(1, state.orders.size)
        assertEquals("#1001", state.orders.first().displayName)
        assertEquals(1, state.orders.first().thumbnails.size)
    }

    @Test
    fun `empty order response becomes empty state`() = runTest {
        val viewModel = OrderHistoryViewModel(
            createUseCases(FakeOrderHistoryRepository(Result.success(emptyList()))),
        )
        advanceUntilIdle()

        assertEquals(OrderHistoryUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `network failure becomes error state`() = runTest {
        val viewModel = OrderHistoryViewModel(
            createUseCases(FakeOrderHistoryRepository(Result.failure(OrderHistoryNetworkException()))),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as OrderHistoryUiState.Error
        assertEquals(R.string.order_history_error_network, state.messageRes)
    }

    @Test
    fun `refresh failure preserves existing content and emits message`() = runTest {
        val repository = FakeOrderHistoryRepository(Result.success(listOf(order())))
        val viewModel = OrderHistoryViewModel(createUseCases(repository))
        advanceUntilIdle()
        repository.result = Result.failure(OrderHistoryNetworkException())

        viewModel.uiEffect.test {
            viewModel.handleIntent(OrderHistoryUiIntent.OnRefresh)
            advanceUntilIdle()

            val effect = awaitItem() as OrderHistoryUiEffect.ShowMessage
            assertEquals(R.string.order_history_error_network, effect.messageRes)
            val state = viewModel.uiState.value as OrderHistoryUiState.Content
            assertEquals(1, state.orders.size)
            assertEquals(false, state.isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `missing Shopify customer id shows customer id error`() = runTest {
        val viewModel = OrderHistoryViewModel(
            createUseCases(
                repository = FakeOrderHistoryRepository(Result.success(listOf(order()))),
                provider = FakeCustomerIdProvider(Result.failure(ShopifyCustomerIdUnavailableException())),
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as OrderHistoryUiState.Error
        assertEquals(R.string.order_history_error_customer_id_unavailable, state.messageRes)
    }

    private fun createUseCases(
        repository: FakeOrderHistoryRepository,
        provider: ICustomerIdProvider = FakeCustomerIdProvider(Result.success(CUSTOMER_ID)),
    ): OrderHistoryUseCases =
        OrderHistoryUseCases(
            getCurrentCustomerId = GetCurrentCustomerIdUseCase(provider),
            getOrderHistory = GetOrderHistoryUseCase(repository),
        )

    private class FakeCustomerIdProvider(
        private val result: Result<Long>,
    ) : ICustomerIdProvider {
        override suspend fun getCurrentCustomerId(): Result<Long> = result
    }

    private class FakeOrderHistoryRepository(
        var result: Result<List<OrderHistory>>,
    ) : IOrderHistoryRepository {
        var lastCustomerId: Long? = null
            private set

        override suspend fun getOrders(customerId: Long): Result<List<OrderHistory>> {
            lastCustomerId = customerId
            return result
        }
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
                trackingNumber = null,
                trackingUrl = null,
                lineItems = listOf(
                    OrderHistoryLineItem(
                        id = 10L,
                        productId = 9091143729380L,
                        variantId = 456L,
                        title = "Silk Top",
                        quantity = 1,
                        price = 120.0,
                        imageUrl = "https://example.com/top.png",
                    )
                ),
            )
    }
}
