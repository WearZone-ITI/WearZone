package com.example.domain.onboarding.repository

import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    fun observeOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit>

    // TODO: Add real auth methods in a future auth task.
}
