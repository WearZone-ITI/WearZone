package com.example.data.local.datastore

import kotlinx.coroutines.flow.Flow

interface IOnboardingPreferencesDataSource {
    fun observeOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
}
