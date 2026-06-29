package com.example.wearzone.data.local.datasource

import kotlinx.coroutines.flow.Flow

interface IOnboardingPreferencesDataSource {
    fun observeOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
}