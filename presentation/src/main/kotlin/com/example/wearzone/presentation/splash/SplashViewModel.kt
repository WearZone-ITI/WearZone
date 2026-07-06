package com.example.wearzone.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val observeOnboardingCompletedUseCase: ObserveOnboardingCompletedUseCase,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SplashUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<SplashUiEffect> = _uiEffect.receiveAsFlow()

    fun handleIntent(intent: SplashUiIntent) {
        when (intent) {
            SplashUiIntent.CheckStartupRouting -> checkStartupRouting()
        }
    }

    private fun checkStartupRouting() {
        viewModelScope.launch {
            try {
                val onboardingCompleted = observeOnboardingCompletedUseCase().first()
                if (!onboardingCompleted) {
                    _uiEffect.send(SplashUiEffect.NavigateToOnboarding)
                } else {
                    val loggedIn = authRepository.isLoggedIn()
                    if (loggedIn) {
                        _uiEffect.send(SplashUiEffect.NavigateToMain)
                    } else {
                        _uiEffect.send(SplashUiEffect.NavigateToLogin)
                    }
                }
            } catch (e: Exception) {
                _uiEffect.send(SplashUiEffect.NavigateToOnboarding)
            }
        }
    }
}
