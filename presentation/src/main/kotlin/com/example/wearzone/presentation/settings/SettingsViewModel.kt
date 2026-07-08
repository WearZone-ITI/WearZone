package com.example.wearzone.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.cart.usecase.ObserveCartItemCountUseCase
import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.domain.settings.usecase.SetLanguageUseCase
import com.example.wearzone.domain.settings.usecase.SetNotificationsEnabledUseCase
import com.example.wearzone.domain.settings.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    observeCartItemCountUseCase: ObserveCartItemCountUseCase,
) : ViewModel() {

    private val settingsState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    private val cartItemCount: StateFlow<Int> = observeCartItemCountUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            0,
        )

    val uiState: StateFlow<SettingsUiState> =
        combine(settingsState, cartItemCount) { state, count ->
            when (state) {
                is SettingsUiState.Content -> state.copy(cartItemCount = count)
                else -> state
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SettingsUiState.Loading,
        )

    private val _uiEffect = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<SettingsUiEffect> = _uiEffect.receiveAsFlow()

    private var settingsJob: Job? = null

    init {
        observeSettings()
    }

    fun handleIntent(intent: SettingsUiIntent) {
        when (intent) {
            SettingsUiIntent.OnBackClicked -> sendEffect(SettingsUiEffect.NavigateBack)
            SettingsUiIntent.OnCartClicked -> sendEffect(SettingsUiEffect.NavigateToCart)
            is SettingsUiIntent.OnThemeModeSelected -> setThemeMode(intent.themeMode)
            is SettingsUiIntent.OnNotificationsToggled -> setNotificationsEnabled(intent.enabled)
            SettingsUiIntent.OnLanguageClicked -> sendEffect(SettingsUiEffect.ShowLanguagePicker)
            is SettingsUiIntent.OnLanguageSelected -> setLanguage(intent.languageCode)
            SettingsUiIntent.OnPrivacyPolicyClicked -> sendEffect(SettingsUiEffect.ShowPrivacyPolicy)
            SettingsUiIntent.OnTermsClicked -> sendEffect(SettingsUiEffect.ShowTerms)
            SettingsUiIntent.OnRetry -> observeSettings()
        }
    }

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = viewModelScope.launch {
            settingsState.value = SettingsUiState.Loading
            observeSettingsPreferencesUseCase()
                .catch {
                    settingsState.value = SettingsUiState.Error(R.string.settings_error_load_failed)
                }
                .collect { preferences ->
                    settingsState.value = SettingsUiState.Content(
                        selectedThemeMode = preferences.themeMode,
                        notificationsEnabled = preferences.notificationsEnabled,
                        languageCode = preferences.languageCode,
                        languageRes = preferences.languageCode.toLanguageRes(),
                    )
                }
        }
    }

    private fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            setThemeModeUseCase(themeMode)
                .onFailure {
                    _uiEffect.send(SettingsUiEffect.ShowError(R.string.settings_error_save_failed))
                }
        }
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            setNotificationsEnabledUseCase(enabled)
                .onFailure {
                    _uiEffect.send(SettingsUiEffect.ShowError(R.string.settings_error_save_failed))
                }
        }
    }

    private fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            setLanguageUseCase(languageCode)
                .onSuccess {
                    _uiEffect.send(SettingsUiEffect.ShowLanguageUpdated)
                }
                .onFailure {
                    _uiEffect.send(SettingsUiEffect.ShowError(R.string.settings_error_save_failed))
                }
        }
    }

    private fun sendEffect(effect: SettingsUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun String.toLanguageRes(): Int =
        when (this) {
            ARABIC_LANGUAGE_CODE -> R.string.settings_language_arabic
            DEFAULT_LANGUAGE_CODE -> R.string.settings_language_english
            else -> R.string.settings_language_english
        }

    private companion object {
        const val DEFAULT_LANGUAGE_CODE = "en"
        const val ARABIC_LANGUAGE_CODE = "ar"
    }
}
