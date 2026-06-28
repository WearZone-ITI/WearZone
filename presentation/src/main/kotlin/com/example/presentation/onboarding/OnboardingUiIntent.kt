package com.example.presentation.onboarding

sealed interface OnboardingUiIntent {
    data object OnGetStartedClicked : OnboardingUiIntent
    data object OnContinueAsGuestClicked : OnboardingUiIntent
    data object OnNextPageClicked : OnboardingUiIntent
    data object OnPreviousPageClicked : OnboardingUiIntent
    data class OnPageSelected(val page: Int) : OnboardingUiIntent
}
