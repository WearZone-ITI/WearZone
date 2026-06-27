package com.wearzone.data.remote.datasource

import com.wearzone.data.remote.dto.AuthUserDto

interface IAuthRemoteDataSource {
    suspend fun loginWithEmail(email: String, password: String): Result<AuthUserDto>

    suspend fun registerWithEmail(email: String, password: String): Result<AuthUserDto>
}
