package com.example.wearzone.presentation.common.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
) : ViewModel() {

    val settingsPreferences: StateFlow<SettingsPreferences> =
        observeSettingsPreferencesUseCase()
            .catch { emit(SettingsPreferences()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsPreferences(),
            )
}
