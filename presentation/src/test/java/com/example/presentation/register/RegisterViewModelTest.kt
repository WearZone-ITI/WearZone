package com.example.presentation.register

import app.cash.turbine.test
import com.example.wearzone.domain.auth.usecase.RegisterUseCase
import com.example.wearzone.presentation.auth.register.RegisterUiEffect
import com.example.wearzone.presentation.auth.register.RegisterUiIntent
import com.example.wearzone.presentation.auth.register.RegisterViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registerUseCase = mockk()
        viewModel = RegisterViewModel(registerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `NameChanged updates name and clears nameError`() = runTest {
        viewModel.handleIntent(RegisterUiIntent.NameChanged("Hend"))
        assertEquals("Hend", viewModel.formState.value.name)
        assertNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `EmailChanged updates email and clears emailError`() = runTest {
        viewModel.handleIntent(RegisterUiIntent.EmailChanged("hend@example.com"))
        assertEquals("hend@example.com", viewModel.formState.value.email)
        assertNull(viewModel.formState.value.emailError)
    }

    @Test
    fun `PasswordChanged updates password and clears passwordError`() = runTest {
        viewModel.handleIntent(RegisterUiIntent.PasswordChanged("Pass@123"))
        assertEquals("Pass@123", viewModel.formState.value.password)
        assertNull(viewModel.formState.value.passwordError)
    }

    @Test
    fun `ConfirmPasswordChanged updates confirmPassword and clears confirmPasswordError`() =
        runTest {
            viewModel.handleIntent(RegisterUiIntent.ConfirmPasswordChanged("Pass@123"))
            assertEquals("Pass@123", viewModel.formState.value.confirmPassword)
            assertNull(viewModel.formState.value.confirmPasswordError)
        }

    @Test
    fun `TermsAccepted updates termsAccepted flag`() = runTest {
        viewModel.handleIntent(RegisterUiIntent.TermsAccepted(true))
        assertTrue(viewModel.formState.value.termsAccepted)
    }

    @Test
    fun `TogglePasswordVisibility flips isPasswordVisible`() = runTest {
        val initial = viewModel.formState.value.isPasswordVisible
        viewModel.handleIntent(RegisterUiIntent.TogglePasswordVisibility)
        assertEquals(!initial, viewModel.formState.value.isPasswordVisible)
    }

    @Test
    fun `ToggleConfirmPasswordVisibility flips isConfirmPasswordVisible`() = runTest {
        val initial = viewModel.formState.value.isConfirmPasswordVisible
        viewModel.handleIntent(RegisterUiIntent.ToggleConfirmPasswordVisibility)
        assertEquals(!initial, viewModel.formState.value.isConfirmPasswordVisible)
    }

    @Test
    fun `NavigateToHomeClicked emits NavigateToHome effect`() = runTest {
        viewModel.effects.test {
            viewModel.handleIntent(RegisterUiIntent.NavigateToHomeClicked)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(RegisterUiEffect.NavigateToHome, awaitItem())
        }
    }
}