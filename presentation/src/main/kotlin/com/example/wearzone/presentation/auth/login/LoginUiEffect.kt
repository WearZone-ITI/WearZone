package com.example.wearzone.presentation.auth.login

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
    data object NavigateToRegister : LoginUiEffect
    data object LaunchGoogleSignIn : LoginUiEffect
    data object NavigateToForgotPassword : LoginUiEffect

    data class ShowSnackbar(val message: String) : LoginUiEffect
}
