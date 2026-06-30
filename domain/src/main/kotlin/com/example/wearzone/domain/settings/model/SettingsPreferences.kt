package com.example.wearzone.domain.settings.model

data class SettingsPreferences(
    val themeMode: ThemeMode = ThemeMode.SystemDefault,
    val notificationsEnabled: Boolean = true,
    val languageCode: String = "en",
)
