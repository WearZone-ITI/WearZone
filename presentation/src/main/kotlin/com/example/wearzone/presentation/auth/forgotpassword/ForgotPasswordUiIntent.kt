package com.example.wearzone.presentation.auth.forgotpassword

sealed interface ForgotPasswordUiIntent {
    data class OnEmailChanged(val email: String) : ForgotPasswordUiIntent
    data object OnSendResetLinkClicked : ForgotPasswordUiIntent
    data object OnBackToLoginClicked : ForgotPasswordUiIntent
}
