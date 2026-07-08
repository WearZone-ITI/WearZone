package com.example.wearzone.presentation.auth.emailverification

import androidx.annotation.StringRes

sealed interface EmailVerificationUiEffect {
    data object NavigateToHome : EmailVerificationUiEffect
    data object NavigateToLogin : EmailVerificationUiEffect
    data class ShowSnackbar(@StringRes val messageRes: Int) : EmailVerificationUiEffect
}
