package com.example.wearzone.presentation.auth.forgotpassword

import androidx.annotation.StringRes

sealed interface ForgotPasswordUiEffect {
    data object NavigateToLogin : ForgotPasswordUiEffect
    data class ShowSnackbar(@StringRes val messageRes: Int) : ForgotPasswordUiEffect
}
