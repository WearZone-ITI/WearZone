package com.example.presentation.auth.login

sealed interface LoginUiIntent {
    data class OnEmailChanged(val email: String) : LoginUiIntent
    data class OnPasswordChanged(val password: String) : LoginUiIntent
    data object OnTogglePasswordVisibility : LoginUiIntent
    data object OnSignInClicked : LoginUiIntent
    data class OnGoogleSignInResult(val idToken: String) : LoginUiIntent
    data object OnGoogleSignInClicked : LoginUiIntent
    data object OnAppleSignInClicked : LoginUiIntent
    data object OnRegisterClicked : LoginUiIntent
    data object OnForgotPasswordClicked : LoginUiIntent
}
