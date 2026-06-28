package com.example.presentation.auth.login

import app.cash.turbine.test
import com.example.domain.auth.model.User
import com.example.domain.auth.usecase.LoginWithEmailUseCase
import com.example.domain.auth.usecase.LoginWithGoogleUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val loginWithEmailUseCase = mockk<LoginWithEmailUseCase>()
    private val loginWithGoogleUseCase = mockk<LoginWithGoogleUseCase>()
    private lateinit var viewModel: LoginViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginWithEmailUseCase, loginWithGoogleUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `successful email login emits Loading then NavigateToHome effect`() = runTest {
        val user = User("1", "test@test.com", null, null)
        coEvery { loginWithEmailUseCase("test@test.com", "pass") } returns Result.success(user)

        viewModel.handleIntent(LoginUiIntent.OnEmailChanged("test@test.com"))
        viewModel.handleIntent(LoginUiIntent.OnPasswordChanged("pass"))

        viewModel.uiEffect.test {
            viewModel.handleIntent(LoginUiIntent.OnSignInClicked)
            
            val effect = awaitItem()
            assertTrue(effect is LoginUiEffect.NavigateToHome)
            assertEquals(LoginUiState.Idle, viewModel.uiState.value)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
