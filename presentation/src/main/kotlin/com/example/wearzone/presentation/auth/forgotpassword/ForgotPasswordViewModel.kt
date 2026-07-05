package com.example.wearzone.presentation.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.usecase.SendPasswordResetEmailUseCase
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
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ForgotPasswordFormState())
    val formState: StateFlow<ForgotPasswordFormState> = _formState.asStateFlow()

    private val _uiEffect = Channel<ForgotPasswordUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun handleIntent(intent: ForgotPasswordUiIntent) {
        when (intent) {
            is ForgotPasswordUiIntent.OnEmailChanged -> {
                _formState.update { it.copy(email = intent.email) }
                if (_uiState.value is ForgotPasswordUiState.Error) {
                    _uiState.value = ForgotPasswordUiState.Idle
                }
            }
            is ForgotPasswordUiIntent.OnSendResetLinkClicked -> {
                sendResetLink()
            }
            is ForgotPasswordUiIntent.OnBackToLoginClicked -> {
                sendEffect(ForgotPasswordUiEffect.NavigateToLogin)
            }
        }
    }

    private fun sendResetLink() {
        val email = _formState.value.email.trim()

        if (email.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error(R.string.forgot_password_empty_email)
            return
        }
        if (!isValidEmail(email)) {
            _uiState.value = ForgotPasswordUiState.Error(R.string.forgot_password_invalid_email)
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            sendPasswordResetEmailUseCase(email)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.EmailSent
                }
                .onFailure { error ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        R.string.forgot_password_failed_send
                    )
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendEffect(effect: ForgotPasswordUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
