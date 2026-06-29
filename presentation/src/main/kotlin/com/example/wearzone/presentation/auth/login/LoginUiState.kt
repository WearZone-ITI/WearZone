package com.example.wearzone.presentation.auth.login

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
)

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}
