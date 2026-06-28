package com.example.presentation.onboarding

sealed interface OnboardingUiState {
    val currentPage: Int

    data class Idle(override val currentPage: Int = 0) : OnboardingUiState
    data class Saving(override val currentPage: Int) : OnboardingUiState
    data class Error(
        val message: String,
        override val currentPage: Int,
    ) : OnboardingUiState
}
