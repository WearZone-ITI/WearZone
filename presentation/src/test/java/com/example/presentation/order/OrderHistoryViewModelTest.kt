package com.example.presentation.order

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.presentation.R
import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.model.OrderHistoryLineItem
import com.example.wearzone.domain.account.model.OrderHistoryNetworkException
import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.account.usecase.CancelOrderUseCase
import com.example.wearzone.domain.account.usecase.GetOrderDetailsUseCase
import com.example.wearzone.domain.account.usecase.GetOrderHistoryUseCase
import com.example.wearzone.domain.account.usecase.OrderHistoryUseCases
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
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
        val viewModel = createViewModel(repository)
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
            createAuthAccessUseCase(),
        )
        advanceUntilIdle()

        assertEquals(OrderHistoryUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `network failure becomes error state`() = runTest {
        val viewModel = OrderHistoryViewModel(
            createUseCases(FakeOrderHistoryRepository(Result.failure(OrderHistoryNetworkException()))),
            createAuthAccessUseCase(),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as OrderHistoryUiState.Error
        assertEquals(R.string.order_history_error_network, state.messageRes)
    }

    @Test
    fun `refresh failure preserves existing content and emits message`() = runTest {
        val repository = FakeOrderHistoryRepository(Result.success(listOf(order())))
        val viewModel = createViewModel(repository)
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
    fun `missing Shopify customer id shows sign in required and skips orders`() = runTest {
        val repository = FakeOrderHistoryRepository(Result.success(listOf(order())))
        val provider = FakeCustomerIdProvider(Result.failure(ShopifyCustomerIdUnavailableException()))
        val viewModel = createViewModel(repository = repository, provider = provider)
        advanceUntilIdle()

        assertEquals(OrderHistoryUiState.SignInRequired, viewModel.uiState.value)
        assertEquals(null, repository.lastCustomerId)
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

    private fun createViewModel(
        repository: FakeOrderHistoryRepository,
        provider: ICustomerIdProvider = FakeCustomerIdProvider(Result.success(CUSTOMER_ID)),
        user: User? = USER,
    ): OrderHistoryViewModel =
        OrderHistoryViewModel(
            orderHistoryUseCases = createUseCases(repository, provider),
            getAuthAccessStateUseCase = createAuthAccessUseCase(provider, user),
        )

    private fun createAuthAccessUseCase(
        provider: ICustomerIdProvider = FakeCustomerIdProvider(Result.success(CUSTOMER_ID)),
        user: User? = USER,
    ): GetAuthAccessStateUseCase =
        GetAuthAccessStateUseCase(
            authRepository = FakeAuthRepository(user),
            customerIdProvider = provider,
        )

    private class FakeCustomerIdProvider(
        private val result: Result<Long>,
    ) : ICustomerIdProvider {
        override suspend fun getCurrentCustomerId(): Result<Long> = result
    }

    private class FakeAuthRepository(
        private val currentUser: User?,
    ) : IAuthRepository {
        override suspend fun loginWithEmail(email: String, password: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun isLoggedIn(): Boolean = currentUser != null

        override suspend fun getCurrentUser(): User? = currentUser

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun register(name: String, email: String, password: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override fun observeOnboardingCompleted(): kotlinx.coroutines.flow.Flow<Boolean> =
            kotlinx.coroutines.flow.flowOf(false)

        override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> =
            Result.success(Unit)
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

        override suspend fun getOrderDetails(orderId: Long, currentCustomerId: Long): Result<OrderDetails> =
            throw UnsupportedOperationException()

        override suspend fun cancelOrder(orderId: Long, reason: OrderCancelReason): Result<OrderDetails> =
            throw UnsupportedOperationException()
    }

    private companion object {
        const val CUSTOMER_ID = 9307871641828L
        val USER = User(
            uid = "firebase-user",
            email = "user@example.com",
            displayName = "Mobile Customer",
            photoUrl = null,
        )

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
