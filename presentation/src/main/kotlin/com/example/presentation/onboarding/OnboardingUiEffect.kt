package com.example.presentation.onboarding

sealed interface OnboardingUiEffect {
    data object NavigateToLogin : OnboardingUiEffect
    data class ShowError(val message: String) : OnboardingUiEffect
    data object ShowGuestModeUnavailable : OnboardingUiEffect
}
