package com.example.wearzone.presentation.auth.emailverification

sealed interface EmailVerificationUiEffect {
    data object NavigateToHome : EmailVerificationUiEffect
    data object NavigateToLogin : EmailVerificationUiEffect
    data class ShowSnackbar(val message: String) : EmailVerificationUiEffect
}
