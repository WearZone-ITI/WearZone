package com.example.wearzone.presentation.auth.register

import androidx.annotation.StringRes

sealed interface RegisterUiEffect {
    data object NavigateToHome : RegisterUiEffect
    data object NavigateToLogin : RegisterUiEffect
    data class NavigateToEmailVerification(val email: String) : RegisterUiEffect
    data class ShowSnackbar(@StringRes val messageRes: Int) : RegisterUiEffect
}
