package com.example.wearzone.presentation.auth.login

sealed interface LoginUiIntent {
    data class OnEmailChanged(val email: String) : LoginUiIntent
    data class OnPasswordChanged(val password: String) : LoginUiIntent
    data object OnTogglePasswordVisibility : LoginUiIntent
    data object OnSignInClicked : LoginUiIntent
    data class OnGoogleSignInResult(val idToken: String) : LoginUiIntent
    data object OnGoogleSignInClicked : LoginUiIntent
    data object OnGuestModeClicked : LoginUiIntent
    data object OnRegisterClicked : LoginUiIntent
    data object OnForgotPasswordClicked : LoginUiIntent
    data object OnJoinAsGuestClicked : LoginUiIntent
}
