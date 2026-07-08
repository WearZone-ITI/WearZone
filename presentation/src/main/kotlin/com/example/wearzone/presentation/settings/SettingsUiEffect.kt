package com.example.wearzone.presentation.settings

import androidx.annotation.StringRes

sealed interface SettingsUiEffect {
    data object NavigateBack : SettingsUiEffect
    data object NavigateToCart : SettingsUiEffect
    data object ShowLanguagePicker : SettingsUiEffect
    data object ShowLanguageUpdated : SettingsUiEffect
    data object ShowPrivacyPolicy : SettingsUiEffect
    data object ShowTerms : SettingsUiEffect
    data class ShowError(@param:StringRes val messageRes: Int) : SettingsUiEffect
}
