package com.example.domain.auth.usecase

import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAuthAccessStateUseCaseTest {

    @Test
    fun `invoke returns Guest when there is no current user`() = runTest {
        val useCase = GetAuthAccessStateUseCase(
            authRepository = FakeAuthRepository(currentUser = null),
            customerIdProvider = FakeCustomerIdProvider(Result.success(1L)),
        )

        assertEquals(AuthAccessState.Guest, useCase())
    }

    @Test
    fun `invoke returns AuthenticatedCustomer when user and customer id exist`() = runTest {
        val useCase = GetAuthAccessStateUseCase(
            authRepository = FakeAuthRepository(),
            customerIdProvider = FakeCustomerIdProvider(Result.success(42L)),
        )

        assertEquals(AuthAccessState.AuthenticatedCustomer(42L), useCase())
    }

    @Test
    fun `invoke returns AuthenticatedMissingCustomerId when customer id lookup fails`() = runTest {
        val useCase = GetAuthAccessStateUseCase(
            authRepository = FakeAuthRepository(),
            customerIdProvider = FakeCustomerIdProvider(Result.failure(IllegalStateException())),
        )

        assertEquals(AuthAccessState.AuthenticatedMissingCustomerId, useCase())
    }

    private class FakeAuthRepository(
        private val currentUser: User? = User("uid", "customer@example.com", "Customer", null),
    ) : IAuthRepository {
        override suspend fun loginWithEmail(email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun isLoggedIn(): Boolean = currentUser != null

        override suspend fun getCurrentUser(): User? = currentUser

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun register(name: String, email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override fun observeOnboardingCompleted(): Flow<Boolean> = flowOf(false)

        override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> =
            Result.success(Unit)
    }

    private class FakeCustomerIdProvider(
        private val result: Result<Long>,
    ) : ICustomerIdProvider {
        override suspend fun getCurrentCustomerId(): Result<Long> = result
    }
}
