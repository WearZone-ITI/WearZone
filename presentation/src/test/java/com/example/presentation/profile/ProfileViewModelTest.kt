package com.example.presentation.profile

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.auth.usecase.LogoutUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase
import com.example.wearzone.presentation.profile.ProfileUiEffect
import com.example.wearzone.presentation.profile.ProfileUiIntent
import com.example.wearzone.presentation.profile.ProfileUiState
import com.example.wearzone.presentation.profile.ProfileViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load shows current user content`() = runTest {
        val repository = FakeAuthRepository(
            currentUser = User("1", "alexandra@example.com", "Alexandra", null),
        )
        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value as ProfileUiState.Content

        assertEquals("Alexandra", state.displayName)
        assertEquals("alexandra@example.com", state.email)
        assertTrue(state.isAuthenticated)
    }

    @Test
    fun `logout clicked emits confirmation effect`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(ProfileUiIntent.OnLogoutClicked)

            assertEquals(ProfileUiEffect.ShowLogoutConfirmation, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout cancelled does not call logout`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.handleIntent(ProfileUiIntent.OnLogoutCancelled)

        assertEquals(0, repository.logoutCalls)
    }

    @Test
    fun `logout confirmed calls use case and emits navigate to login`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.uiEffect.test {
            viewModel.handleIntent(ProfileUiIntent.OnLogoutConfirmed)

            assertEquals(ProfileUiEffect.NavigateToLogin, awaitItem())
            assertEquals(1, repository.logoutCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout failure emits localized error effect`() = runTest {
        val repository = FakeAuthRepository(logoutResult = Result.failure(IllegalStateException()))
        val viewModel = createViewModel(repository)

        viewModel.uiEffect.test {
            viewModel.handleIntent(ProfileUiIntent.OnLogoutConfirmed)

            val effect = awaitItem() as ProfileUiEffect.ShowError
            assertEquals(R.string.profile_logout_failed, effect.messageRes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cart item count is observed and displayed in profile`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value as ProfileUiState.Content

        assertEquals(0, state.cartItemCount)
    }

    @Test
    fun `guest load shows guest state`() = runTest {
        val repository = FakeAuthRepository(currentUser = null)
        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value is ProfileUiState.Guest)
    }

    private fun createViewModel(
        repository: FakeAuthRepository = FakeAuthRepository(),
        customerIdResult: Result<Long> = Result.success(1L),
    ): ProfileViewModel {
        val cartRepository = FakeCartRepository()
        val customerIdProvider = FakeCustomerIdProvider(customerIdResult)
        return ProfileViewModel(
            getAuthAccessStateUseCase = GetAuthAccessStateUseCase(
                repository,
                customerIdProvider,
            ),
            getCurrentUserUseCase = GetCurrentUserUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
            observeCartUseCase = ObserveCartUseCase(cartRepository),
            getOrderHistoryUseCase = mockk { coEvery { this@mockk.invoke(any()) } returns Result.success(emptyList()) },
            getCurrentCustomerIdUseCase = GetCurrentCustomerIdUseCase(customerIdProvider)
        )
    }

    private class FakeCustomerIdProvider(
        private val result: Result<Long>,
    ) : ICustomerIdProvider {
        override suspend fun getCurrentCustomerId(): Result<Long> = result
    }

    private class FakeAuthRepository(
        private val currentUser: User? = User("1", "alexandra@example.com", "Alexandra", null),
        private val logoutResult: Result<Unit> = Result.success(Unit),
    ) : IAuthRepository {
        var logoutCalls = 0
            private set

        override suspend fun loginWithEmail(email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun isLoggedIn(): Boolean = currentUser != null

        override suspend fun getCurrentUser(): User? = currentUser

        override suspend fun logout(): Result<Unit> {
            logoutCalls += 1
            return logoutResult
        }

        override suspend fun register(name: String, email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun checkEmailVerified(): Result<Boolean> = Result.success(true)

        override suspend fun sendEmailVerification(): Result<Unit> = Result.success(Unit)

        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)

        override fun observeOnboardingCompleted(): Flow<Boolean> = flowOf(false)

        override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> =
            Result.success(Unit)
    }

    private class FakeCartRepository(
        private val items: List<CartItem> = emptyList(),
    ) : ICartRepository {
        override fun observeCart(): Flow<List<CartItem>> = flowOf(items)

        override suspend fun addToCart(item: CartItem): DataResult<Unit> = DataResult.Success(Unit)

        override suspend fun removeFromCart(variantId: String): DataResult<Unit> =
            DataResult.Success(Unit)

        override suspend fun updateQuantity(variantId: String, quantity: Int): DataResult<Unit> =
            DataResult.Success(Unit)

        override suspend fun clearCart(): DataResult<Unit> = DataResult.Success(Unit)
    }
}
