package com.example.wearzone.presentation.auth.login

import androidx.annotation.StringRes


data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
)

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(
        @StringRes val messageRes: Int,
        val showAsWarning: Boolean = false,
    ) : LoginUiState
}
