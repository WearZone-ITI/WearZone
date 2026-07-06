package com.example.wearzone.presentation.auth.emailverification

data class EmailVerificationScreenState(
    val email: String = "",
    val resendCooldownSeconds: Int = 0,
    val isChecking: Boolean = false,
    val isResending: Boolean = false,
)

sealed interface EmailVerificationUiState {
    data object NotVerified : EmailVerificationUiState
    data object Verified : EmailVerificationUiState
    data class Error(val message: String) : EmailVerificationUiState
}
