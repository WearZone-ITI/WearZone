package com.example.wearzone.presentation.auth.login

import androidx.annotation.StringRes

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
    data object NavigateToRegister : LoginUiEffect
    data object LaunchGoogleSignIn : LoginUiEffect
    data class NavigateToEmailVerification(val email: String) : LoginUiEffect
    data object NavigateToForgotPassword : LoginUiEffect

    data class ShowSnackbar(@StringRes val messageRes: Int) : LoginUiEffect
}
