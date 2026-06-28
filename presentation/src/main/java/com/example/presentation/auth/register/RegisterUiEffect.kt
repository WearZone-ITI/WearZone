package com.example.presentation.auth.register

import com.example.domain.util.ValidationError

sealed interface RegisterUiEffect {
    data object NavigateToHome : RegisterUiEffect
    data object NavigateToLogin : RegisterUiEffect
    data class ShowSnackbar(val error: ValidationError?) : RegisterUiEffect
}
