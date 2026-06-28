package com.example.domain.auth.repository

import com.example.domain.auth.model.User

interface IAuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun loginWithGoogleCredential(idToken: String): Result<User>
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun register(name: String, email: String, password: String): Result<User>
}
