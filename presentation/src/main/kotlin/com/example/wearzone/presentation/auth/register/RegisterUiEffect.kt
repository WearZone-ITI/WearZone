package com.example.wearzone.presentation.auth.register

import com.example.wearzone.domain.common.ValidationError

sealed interface RegisterUiEffect {
    data object NavigateToHome : RegisterUiEffect
    data object NavigateToLogin : RegisterUiEffect
    data class ShowSnackbar(val error: ValidationError?) : RegisterUiEffect
}
