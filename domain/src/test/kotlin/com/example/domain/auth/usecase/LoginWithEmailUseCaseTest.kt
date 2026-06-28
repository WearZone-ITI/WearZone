package com.example.domain.auth.usecase

import com.example.domain.auth.model.User
import com.example.domain.auth.repository.IAuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginWithEmailUseCaseTest {

    private val authRepository = mockk<IAuthRepository>()
    private val useCase = LoginWithEmailUseCase(authRepository)

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val user = User("uid", "test@test.com", "Test", null)
        coEvery { authRepository.loginWithEmail("test@test.com", "password") } returns Result.success(user)

        val result = useCase("test@test.com", "password")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = Exception("Login Failed")
        coEvery { authRepository.loginWithEmail("test@test.com", "password") } returns Result.failure(error)

        val result = useCase("test@test.com", "password")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
