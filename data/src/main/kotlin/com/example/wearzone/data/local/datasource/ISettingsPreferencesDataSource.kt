package com.example.wearzone.data.local.datasource

import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ISettingsPreferencesDataSource {
    fun observeSettingsPreferences(): Flow<SettingsPreferences>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setLanguage(languageCode: String)
}
