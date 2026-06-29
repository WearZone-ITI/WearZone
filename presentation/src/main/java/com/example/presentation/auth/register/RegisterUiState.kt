package com.example.presentation.auth.register

import com.example.domain.util.ValidationError

/**
 * Represents real-time state of each form field,
 * kept separate from loading/error state to avoid full recomposition.
 */
data class RegisterFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
)

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data class Success(val user: RegisteredUserUiModel) : RegisterUiState
    data class Error(val message: ValidationError?) : RegisterUiState
}

data class RegisteredUserUiModel(
    val email: String,
    val displayName: String,
)