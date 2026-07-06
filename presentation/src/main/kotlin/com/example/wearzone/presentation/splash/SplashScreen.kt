package com.example.wearzone.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.wearzone.presentation.common.WearZoneAnimatedLoader
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.handleIntent(SplashUiIntent.CheckStartupRouting)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                SplashUiEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                SplashUiEffect.NavigateToLogin -> onNavigateToLogin()
                SplashUiEffect.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)),
        contentAlignment = Alignment.Center
    ) {
        WearZoneAnimatedLoader()
    }
}
