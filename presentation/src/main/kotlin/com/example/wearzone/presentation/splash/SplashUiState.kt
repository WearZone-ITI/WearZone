package com.example.wearzone.presentation.splash

sealed interface SplashUiState {
    data object Loading : SplashUiState
}
