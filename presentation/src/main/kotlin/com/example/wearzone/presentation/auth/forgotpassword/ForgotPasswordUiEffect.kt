package com.example.wearzone.presentation.auth.forgotpassword

sealed interface ForgotPasswordUiEffect {
    data object NavigateToLogin : ForgotPasswordUiEffect
    data class ShowSnackbar(val message: String) : ForgotPasswordUiEffect
}
