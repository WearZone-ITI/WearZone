package com.example.wearzone.presentation.onboarding

sealed interface OnboardingUiEffect {
    data object NavigateToLogin : OnboardingUiEffect
    data object NavigateToGuest : OnboardingUiEffect
}
