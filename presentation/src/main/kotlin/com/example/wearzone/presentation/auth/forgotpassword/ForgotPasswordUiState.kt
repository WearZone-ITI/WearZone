package com.example.wearzone.presentation.auth.forgotpassword

data class ForgotPasswordFormState(
    val email: String = "",
)

sealed interface ForgotPasswordUiState {
    data object Idle : ForgotPasswordUiState
    data object Loading : ForgotPasswordUiState
    data object EmailSent : ForgotPasswordUiState
    data class Error(val messageRes: Int) : ForgotPasswordUiState
}
