package com.example.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.auth.usecase.RegisterUseCase
import com.example.domain.util.ValidationException
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
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    private val _effects = Channel<RegisterUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: RegisterUiIntent) {
        when (intent) {
            is RegisterUiIntent.NameChanged -> _formState.update {
                it.copy(name = intent.value, nameError = null)
            }

            is RegisterUiIntent.EmailChanged -> _formState.update {
                it.copy(email = intent.value, emailError = null)
            }

            is RegisterUiIntent.PasswordChanged -> _formState.update {
                it.copy(password = intent.value, passwordError = null)
            }

            is RegisterUiIntent.ConfirmPasswordChanged -> _formState.update {
                it.copy(confirmPassword = intent.value, confirmPasswordError = null)
            }

            is RegisterUiIntent.TermsAccepted -> _formState.update {
                it.copy(termsAccepted = intent.accepted)
            }

            is RegisterUiIntent.TogglePasswordVisibility -> _formState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            is RegisterUiIntent.ToggleConfirmPasswordVisibility -> _formState.update {
                it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
            }

            is RegisterUiIntent.SubmitRegister -> submitRegister()

            is RegisterUiIntent.NavigateToHomeClicked -> viewModelScope.launch {
                _effects.send(RegisterUiEffect.NavigateToHome)
            }
        }
    }

    private fun submitRegister() {
        val form = _formState.value
        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            val result = registerUseCase(
                name = form.name,
                email = form.email,
                password = form.password,
                confirmPassword = form.confirmPassword,
                termsAccepted = form.termsAccepted,
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.value = RegisterUiState.Success(
                        RegisteredUserUiModel(
                            email = user.email,
                            displayName = user.displayName,
                        ),
                    )
                    _effects.send(RegisterUiEffect.NavigateToHome)
                },
                onFailure = { throwable ->
                    val message = (throwable as? ValidationException)?.error
                    _uiState.value = RegisterUiState.Error(message)
                    _effects.send(RegisterUiEffect.ShowSnackbar(message))
                },
            )
        }
    }
}