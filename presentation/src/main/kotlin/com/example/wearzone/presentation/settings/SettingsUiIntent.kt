package com.example.wearzone.presentation.settings

import com.example.wearzone.domain.settings.model.ThemeMode

sealed interface SettingsUiIntent {
    data object OnBackClicked : SettingsUiIntent
    data object OnCartClicked : SettingsUiIntent
    data class OnThemeModeSelected(val themeMode: ThemeMode) : SettingsUiIntent
    data class OnNotificationsToggled(val enabled: Boolean) : SettingsUiIntent
    data object OnLanguageClicked : SettingsUiIntent
    data class OnLanguageSelected(val languageCode: String) : SettingsUiIntent
    data object OnPrivacyPolicyClicked : SettingsUiIntent
    data object OnTermsClicked : SettingsUiIntent
    data object OnRetry : SettingsUiIntent
}
