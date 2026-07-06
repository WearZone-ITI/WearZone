package com.example.wearzone.presentation.auth.emailverification

sealed interface EmailVerificationUiIntent {
    data object OnResendClicked : EmailVerificationUiIntent
    data object OnCheckStatusClicked : EmailVerificationUiIntent
    data object OnLogoutClicked : EmailVerificationUiIntent
}
