package com.example.wearzone.presentation.settings

import androidx.annotation.StringRes
import com.example.presentation.R
import com.example.wearzone.domain.settings.model.ThemeMode

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Content(
        val selectedThemeMode: ThemeMode,
        val notificationsEnabled: Boolean,
        val languageCode: String = "en",
        @param:StringRes val languageRes: Int = R.string.settings_language_english,
        val cartItemCount: Int = 0,
    ) : SettingsUiState

    data class Error(@param:StringRes val messageRes: Int) : SettingsUiState
}
