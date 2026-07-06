package com.example.wearzone.data.local.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PayMockLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : IPayMockLocalDataSource {

    override suspend fun saveApiKey(key: String) {
        dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }

    override suspend fun getApiKey(): String? {
        return dataStore.data.map { it[API_KEY] }.firstOrNull()
    }

    override suspend fun clearApiKey() {
        dataStore.edit { preferences ->
            preferences.remove(API_KEY)
        }
    }

    private companion object {
        val API_KEY = stringPreferencesKey("paymock_api_key")
    }
}
