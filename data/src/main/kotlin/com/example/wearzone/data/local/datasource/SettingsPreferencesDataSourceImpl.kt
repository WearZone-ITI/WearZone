package com.example.wearzone.data.local.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsPreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : ISettingsPreferencesDataSource {

    override fun observeSettingsPreferences(): Flow<SettingsPreferences> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                SettingsPreferences(
                    themeMode = preferences[THEME_MODE_KEY].toThemeMode(),
                    notificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: true,
                    languageCode = preferences[LANGUAGE_CODE_KEY] ?: DEFAULT_LANGUAGE_CODE,
                )
            }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    override suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE_KEY] = languageCode
        }
    }

    override suspend fun setCustomerId(id: Long?) {
        dataStore.edit { preferences ->
            if (id == null) {
                preferences.remove(CUSTOMER_ID_KEY)
            } else {
                preferences[CUSTOMER_ID_KEY] = id
            }
        }
    }

    override fun observeCustomerId(): Flow<Long?> = dataStore.data.map { it[CUSTOMER_ID_KEY] }
    private fun String?.toThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.SystemDefault

    private companion object {
        const val DEFAULT_LANGUAGE_CODE = "en"
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val LANGUAGE_CODE_KEY = stringPreferencesKey("language_code")
        val CUSTOMER_ID_KEY = longPreferencesKey("customer_id")
    }
}
