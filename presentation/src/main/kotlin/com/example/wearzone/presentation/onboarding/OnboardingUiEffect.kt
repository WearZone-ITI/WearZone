package com.example.wearzone.presentation.onboarding

sealed interface OnboardingUiEffect {
    data object NavigateToLogin : OnboardingUiEffect
}
