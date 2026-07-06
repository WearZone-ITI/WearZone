package com.example.wearzone.presentation.splash

sealed interface SplashUiEffect {
    data object NavigateToOnboarding : SplashUiEffect
    data object NavigateToLogin : SplashUiEffect
    data object NavigateToMain : SplashUiEffect
}
