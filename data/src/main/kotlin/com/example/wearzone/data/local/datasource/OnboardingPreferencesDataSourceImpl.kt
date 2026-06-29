package com.example.wearzone.data.local.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class OnboardingPreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : IOnboardingPreferencesDataSource {

    override fun observeOnboardingCompleted(): Flow<Boolean> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[HAS_SEEN_ONBOARDING_KEY] ?: false
            }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING_KEY] = completed
        }
    }

    private companion object {
        val HAS_SEEN_ONBOARDING_KEY = booleanPreferencesKey("has_seen_onboarding")
    }
}