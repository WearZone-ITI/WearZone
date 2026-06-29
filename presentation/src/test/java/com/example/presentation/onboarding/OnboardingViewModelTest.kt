package com.example.presentation.onboarding

import com.example.wearzone.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import com.example.wearzone.domain.onboarding.usecase.SetOnboardingCompletedUseCase
import com.example.presentation.MainDispatcherRule
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.presentation.onboarding.OnboardingUiEffect
import com.example.wearzone.presentation.onboarding.OnboardingUiIntent
import com.example.wearzone.presentation.onboarding.OnboardingUiState
import com.example.wearzone.presentation.onboarding.OnboardingViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is Idle`() {
        val viewModel = createViewModel()

        assertEquals(OnboardingUiState.Idle(), viewModel.uiState.value)
    }

    @Test
    fun `Get Started on final onboarding page saves completion and emits NavigateToLogin`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.handleIntent(OnboardingUiIntent.OnPageSelected(2))
        viewModel.handleIntent(OnboardingUiIntent.OnGetStartedClicked)

        assertTrue(repository.savedCompletion)
        assertEquals(OnboardingUiEffect.NavigateToLogin, viewModel.uiEffect.first())
        assertEquals(OnboardingUiState.Idle(2), viewModel.uiState.value)
    }

    @Test
    fun `write failure shows inline error and does not navigate`() = runTest {
        val repository = FakeAuthRepository(writeError = IllegalStateException("Disk is full"))
        val viewModel = createViewModel(repository)

        viewModel.handleIntent(OnboardingUiIntent.OnGetStartedClicked)

        assertTrue(repository.saveAttempted)
        assertEquals(
            OnboardingUiState.Error(R.string.onboarding_error_unable_to_continue, 0),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `Continue as Guest emits NavigateToGuest and does not save`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.handleIntent(OnboardingUiIntent.OnContinueAsGuestClicked)

        assertFalse(repository.saveAttempted)
        assertEquals(OnboardingUiEffect.NavigateToGuest, viewModel.uiEffect.first())
        assertEquals(OnboardingUiState.Idle(), viewModel.uiState.value)
    }

    @Test
    fun `page navigation updates current onboarding page`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(OnboardingUiIntent.OnNextPageClicked)
        assertEquals(OnboardingUiState.Idle(1), viewModel.uiState.value)

        viewModel.handleIntent(OnboardingUiIntent.OnPageSelected(2))
        assertEquals(OnboardingUiState.Idle(2), viewModel.uiState.value)

        viewModel.handleIntent(OnboardingUiIntent.OnPreviousPageClicked)
        assertEquals(OnboardingUiState.Idle(1), viewModel.uiState.value)
    }

    private fun createViewModel(
        repository: FakeAuthRepository = FakeAuthRepository(),
    ): OnboardingViewModel =
        OnboardingViewModel(
            observeOnboardingCompletedUseCase = ObserveOnboardingCompletedUseCase(repository),
            setOnboardingCompletedUseCase = SetOnboardingCompletedUseCase(repository),
        )

    private class FakeAuthRepository(
        private val writeError: Throwable? = null,
    ) : IAuthRepository {
        private val completed = MutableStateFlow(false)
        var saveAttempted = false
            private set
        var savedCompletion = false
            private set

        override suspend fun loginWithEmail(
            email: String,
            password: String
        ): Result<User> {
            TODO("Not yet implemented")
        }

        override suspend fun loginWithGoogleCredential(idToken: String): Result<User> {
            TODO("Not yet implemented")
        }

        override suspend fun isLoggedIn(): Boolean {
            TODO("Not yet implemented")
        }

        override suspend fun getCurrentUser(): User? {
            TODO("Not yet implemented")
        }

        override suspend fun register(
            name: String,
            email: String,
            password: String
        ): Result<User> {
            TODO("Not yet implemented")
        }

        override fun observeOnboardingCompleted(): Flow<Boolean> = completed

        override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> {
            saveAttempted = true
            writeError?.let { return Result.failure(it) }
            this.completed.value = completed
            savedCompletion = completed
            return Result.success(Unit)
        }
    }
}
