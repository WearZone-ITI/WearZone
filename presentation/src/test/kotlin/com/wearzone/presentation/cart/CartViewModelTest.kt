package com.wearzone.presentation.cart

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.cart.usecase.ClearCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.cart.usecase.RemoveFromCartUseCase
import com.example.wearzone.domain.cart.usecase.UpdateCartQuantityUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.presentation.cart.CartUiIntent
import com.example.wearzone.presentation.cart.CartUiState
import com.example.wearzone.presentation.cart.CartViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state with authenticated user and items shows content`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cartItem = createTestCartItem("v1", "p1", "Shirt", quantity = 2)
            val repository = FakeCartRepository(items = listOf(cartItem))
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertTrue(awaitItem() is CartUiState.Loading)

                val state = awaitItem() as CartUiState.Content
                assertEquals(2, state.itemCount)
                assertEquals(1, state.items.size)
                assertTrue(state.items[0].title.contains("Shirt"))
            }
        }

    @Test
    fun `initial state with unauthenticated user shows login required`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeCartRepository(authenticated = false)
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertTrue(awaitItem() is CartUiState.Loading)
                assertEquals(CartUiState.LoginRequired, awaitItem())
            }
        }

    @Test
    fun `empty cart shows empty state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeCartRepository(items = emptyList())
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertTrue(awaitItem() is CartUiState.Loading)
                assertEquals(CartUiState.Empty, awaitItem())
            }
        }

    @Test
    fun `increase quantity updates state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cartItem = createTestCartItem("v1", "p1", "Shirt", quantity = 1)
            val repository = FakeCartRepository(items = listOf(cartItem))
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                awaitItem() // Loading
                awaitItem() // Content (quantity = 1)

                viewModel.handleIntent(CartUiIntent.OnIncreaseQuantity("v1"))

                val updated = awaitItem() as CartUiState.Content
                assertEquals(2, updated.itemCount)
            }
        }

    @Test
    fun `decrease quantity updates state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cartItem = createTestCartItem("v1", "p1", "Shirt", quantity = 2)
            val repository = FakeCartRepository(items = listOf(cartItem))
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                awaitItem() // Loading
                awaitItem() // Content (quantity = 2)

                viewModel.handleIntent(CartUiIntent.OnDecreaseQuantity("v1"))

                val updated = awaitItem() as CartUiState.Content
                assertEquals(1, updated.itemCount)
            }
        }

    @Test
    fun `remove item confirmed removes from cart`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cartItem = createTestCartItem("v1", "p1", "Shirt")
            val repository = FakeCartRepository(items = listOf(cartItem))
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                awaitItem() // Loading
                awaitItem() // Content

                viewModel.handleIntent(CartUiIntent.OnRemoveItemConfirmed("v1"))

                assertEquals(CartUiState.Empty, awaitItem())
            }
        }

    @Test
    fun `clear cart confirmed clears all items`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val items = listOf(
                createTestCartItem("v1", "p1", "Shirt"),
                createTestCartItem("v2", "p2", "Pants"),
            )
            val repository = FakeCartRepository(items = items)
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                awaitItem() // Loading
                awaitItem() // Content

                viewModel.handleIntent(CartUiIntent.OnClearCartConfirmed)

                assertEquals(CartUiState.Empty, awaitItem())
            }
        }

    @Test
    fun `subtotal calculation is correct`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val items = listOf(
                createTestCartItem("v1", "p1", "Shirt", price = 100.0, quantity = 2),
                createTestCartItem("v2", "p2", "Pants", price = 200.0, quantity = 1),
            )
            val repository = FakeCartRepository(items = items)
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                awaitItem() // Loading
                val state = awaitItem() as CartUiState.Content
                assertTrue(state.subtotal.contains("400.00"))
            }
        }

    private fun createViewModel(
        repository: FakeCartRepository,
    ): CartViewModel = CartViewModel(
        observeCartUseCase = ObserveCartUseCase(repository),
        updateCartQuantityUseCase = UpdateCartQuantityUseCase(repository),
        removeFromCartUseCase = RemoveFromCartUseCase(repository),
        clearCartUseCase = ClearCartUseCase(repository),
        getCurrentUserUseCase = GetCurrentUserUseCase(
            FakeAuthRepository(authenticated = repository.isAuthenticated())
        ),
    )

    private fun createTestCartItem(
        variantId: String,
        productId: String,
        title: String,
        price: Double = 100.0,
        quantity: Int = 1,
        maxQuantity: Int = 10,
    ): CartItem = CartItem(
        variantId = variantId,
        productId = productId,
        title = title,
        vendor = "TestVendor",
        price = price,
        currencyCode = "USD",
        quantity = quantity,
        maxQuantity = maxQuantity,
        imageUrl = "https://example.com/image.jpg",
        size = "M",
    )

    private class FakeCartRepository(
        private val items: List<CartItem> = emptyList(),
        private val authenticated: Boolean = true,
        private val updateQuantityResult: Result<Unit> = Result.success(Unit),
    ) : ICartRepository {
        private var currentItems = items.toMutableList()

        override fun observeCart(): Flow<List<CartItem>> = flowOf(currentItems)

        override suspend fun addToCart(item: CartItem): DataResult<Unit> =
            Result.success(Unit).fold(
                onSuccess = { DataResult.Success(Unit) },
                onFailure = { DataResult.Error(DomainError.Unknown(it)) }
            )

        override suspend fun removeFromCart(variantId: String): DataResult<Unit> {
            currentItems.removeAll { it.variantId == variantId }
            return DataResult.Success(Unit)
        }

        override suspend fun updateQuantity(variantId: String, quantity: Int): DataResult<Unit> {
            return updateQuantityResult.fold(
                onSuccess = {
                    currentItems.replaceAll { item ->
                        if (item.variantId == variantId) item.copy(quantity = quantity) else item
                    }
                    DataResult.Success(Unit)
                },
                onFailure = { DataResult.Error(DomainError.Unknown(it)) }
            )
        }

        override suspend fun clearCart(): DataResult<Unit> {
            currentItems.clear()
            return DataResult.Success(Unit)
        }

        fun isAuthenticated(): Boolean = authenticated
    }

    private class FakeAuthRepository(
        private val authenticated: Boolean = true,
    ) : IAuthRepository {
        override suspend fun loginWithEmail(email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun isLoggedIn(): Boolean = authenticated

        override suspend fun getCurrentUser(): User? =
            if (authenticated) User("1", "test@example.com", "Test User", null) else null

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun register(name: String, email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override fun observeOnboardingCompleted(): Flow<Boolean> = flowOf(false)

        override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> =
            Result.success(Unit)
    }
}