package com.example.wearzone.domain.auth.usecase

import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository

class LoginWithEmailUseCase(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.loginWithEmail(email, password)
    }
}
