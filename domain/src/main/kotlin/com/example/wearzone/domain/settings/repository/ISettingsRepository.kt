package com.example.wearzone.domain.settings.repository

import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun observeSettingsPreferences(): Flow<SettingsPreferences>
    suspend fun setThemeMode(themeMode: ThemeMode): Result<Unit>
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>
    suspend fun setLanguage(languageCode: String): Result<Unit>
}