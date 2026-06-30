package com.example.domain.auth.usecase

import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.LogoutUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoutUseCaseTest {

    private val authRepository = mockk<IAuthRepository>()
    private val useCase = LogoutUseCase(authRepository)

    @Test
    fun `invoke returns success when repository logout succeeds`() = runTest {
        coEvery { authRepository.logout() } returns Result.success(Unit)

        val result = useCase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository logout fails`() = runTest {
        val error = IllegalStateException("Logout failed")
        coEvery { authRepository.logout() } returns Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
