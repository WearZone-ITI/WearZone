package com.example.wearzone.presentation.splash

sealed interface SplashUiIntent {
    data object CheckStartupRouting : SplashUiIntent
}
