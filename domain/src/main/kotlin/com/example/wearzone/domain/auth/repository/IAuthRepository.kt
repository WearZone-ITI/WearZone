package com.example.wearzone.domain.auth.repository

import com.example.wearzone.domain.auth.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun loginWithGoogleCredential(idToken: String): Result<User>
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun logout(): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<User>
    fun observeOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit>

}
