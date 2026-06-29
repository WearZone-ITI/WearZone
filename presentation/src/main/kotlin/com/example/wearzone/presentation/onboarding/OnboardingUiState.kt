package com.example.wearzone.presentation.onboarding

import androidx.annotation.StringRes

sealed interface OnboardingUiState {
    val currentPage: Int

    data class Idle(override val currentPage: Int = 0) : OnboardingUiState
    data class Saving(override val currentPage: Int) : OnboardingUiState
    data class Error(
        @param:StringRes val messageRes: Int,
        override val currentPage: Int,
    ) : OnboardingUiState
}
