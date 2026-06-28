package com.example.domain.auth.repository

import com.example.domain.auth.model.User

interface IAuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<User>
}
