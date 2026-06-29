package com.example.wearzone.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import com.example.wearzone.domain.onboarding.usecase.SetOnboardingCompletedUseCase
import com.example.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LAST_ONBOARDING_PAGE = 2

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    observeOnboardingCompletedUseCase: ObserveOnboardingCompletedUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
) : ViewModel() {

    val hasCompletedOnboarding: StateFlow<Boolean?> =
        observeOnboardingCompletedUseCase()
            .map<Boolean, Boolean?> { it }
            .catch { emit(false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<OnboardingUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<OnboardingUiEffect> = _uiEffect.receiveAsFlow()

    fun handleIntent(intent: OnboardingUiIntent) {
        when (intent) {
            OnboardingUiIntent.OnGetStartedClicked -> completeOnboarding()
            OnboardingUiIntent.OnContinueAsGuestClicked -> navigateToGuest()
            OnboardingUiIntent.OnNextPageClicked -> showNextPage()
            OnboardingUiIntent.OnPreviousPageClicked -> showPreviousPage()
            is OnboardingUiIntent.OnPageSelected -> selectPage(intent.page)
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val page = _uiState.value.currentPage
            _uiState.value = OnboardingUiState.Saving(page)
            val result = setOnboardingCompletedUseCase(true)
            result
                .onSuccess {
                    _uiState.value = OnboardingUiState.Idle(page)
                    _uiEffect.send(OnboardingUiEffect.NavigateToLogin)
                }
                .onFailure {
                    _uiState.value = OnboardingUiState.Error(
                        messageRes = R.string.onboarding_error_unable_to_continue,
                        currentPage = page,
                    )
                }
        }
    }

    private fun navigateToGuest() {
        viewModelScope.launch {
            _uiEffect.send(OnboardingUiEffect.NavigateToGuest)
        }
    }

    private fun showNextPage() {
        selectPage((_uiState.value.currentPage + 1).coerceAtMost(LAST_ONBOARDING_PAGE))
    }

    private fun showPreviousPage() {
        selectPage((_uiState.value.currentPage - 1).coerceAtLeast(0))
    }

    private fun selectPage(page: Int) {
        _uiState.value = OnboardingUiState.Idle(page.coerceIn(0, LAST_ONBOARDING_PAGE))
    }
}
