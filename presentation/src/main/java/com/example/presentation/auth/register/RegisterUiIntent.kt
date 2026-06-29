package com.example.presentation.auth.register

sealed interface RegisterUiIntent {
    data class NameChanged(val value: String) : RegisterUiIntent
    data class EmailChanged(val value: String) : RegisterUiIntent
    data class PasswordChanged(val value: String) : RegisterUiIntent
    data class ConfirmPasswordChanged(val value: String) : RegisterUiIntent
    data class TermsAccepted(val accepted: Boolean) : RegisterUiIntent
    data object TogglePasswordVisibility : RegisterUiIntent
    data object ToggleConfirmPasswordVisibility : RegisterUiIntent
    data object SubmitRegister : RegisterUiIntent
    data object NavigateToHomeClicked : RegisterUiIntent
}
