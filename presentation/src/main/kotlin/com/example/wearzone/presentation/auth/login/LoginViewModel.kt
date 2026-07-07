package com.example.wearzone.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.usecase.CheckEmailVerifiedUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithEmailUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val checkEmailVerifiedUseCase: CheckEmailVerifiedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    private val _uiEffect = Channel<LoginUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun handleIntent(intent: LoginUiIntent) {
        when (intent) {
            is LoginUiIntent.OnEmailChanged -> {
                _formState.update { it.copy(email = intent.email) }
            }
            is LoginUiIntent.OnPasswordChanged -> {
                _formState.update { it.copy(password = intent.password) }
            }
            is LoginUiIntent.OnTogglePasswordVisibility -> {
                _formState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginUiIntent.OnSignInClicked -> {
                loginWithEmail()
            }
            is LoginUiIntent.OnGoogleSignInClicked -> {
                sendEffect(LoginUiEffect.LaunchGoogleSignIn)
            }
            is LoginUiIntent.OnGoogleSignInResult -> {
                loginWithGoogle(intent.idToken)
            }
            is LoginUiIntent.OnGuestModeClicked -> {
                sendEffect(LoginUiEffect.NavigateToHome)
            }
            is LoginUiIntent.OnRegisterClicked -> {
                sendEffect(LoginUiEffect.NavigateToRegister)
            }
            is LoginUiIntent.OnForgotPasswordClicked -> {
                sendEffect(LoginUiEffect.NavigateToForgotPassword)
            }
            is LoginUiIntent.OnJoinAsGuestClicked -> sendEffect(LoginUiEffect.NavigateToHome)
        }
    }

    private fun loginWithEmail() {
        val currentForm = _formState.value
        if (currentForm.email.isBlank() || currentForm.password.isBlank()) {
            sendEffect(LoginUiEffect.ShowSnackbar("Please fill in all fields"))
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginWithEmailUseCase(currentForm.email, currentForm.password)
                .onSuccess { user ->
                    checkEmailVerifiedUseCase()
                        .onSuccess { isVerified ->
                            _uiState.value = LoginUiState.Idle
                            if (isVerified) {
                                sendEffect(LoginUiEffect.NavigateToHome)
                            } else {
                                sendEffect(LoginUiEffect.NavigateToEmailVerification(user.email))
                            }
                        }
                        .onFailure { error ->
                            _uiState.value = LoginUiState.Error(error.localizedMessage ?: "Verification check failed")
                        }
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(error.localizedMessage ?: "Login failed")
                }
        }

    }

    private fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginWithGoogleUseCase(idToken)
                .onSuccess { user ->
                    checkEmailVerifiedUseCase()
                        .onSuccess { isVerified ->
                            _uiState.value = LoginUiState.Idle
                            if (isVerified) {
                                sendEffect(LoginUiEffect.NavigateToHome)
                            } else {
                                sendEffect(LoginUiEffect.NavigateToEmailVerification(user.email))
                            }
                        }
                        .onFailure { error ->
                            _uiState.value = LoginUiState.Error(error.localizedMessage ?: "Verification check failed")
                        }
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(error.localizedMessage ?: "Google Sign-In failed")
                }
        }
    }


    private fun sendEffect(effect: LoginUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
