package com.example.wearzone.domain.auth.usecase

import com.example.wearzone.domain.auth.repository.IAuthRepository

class SendPasswordResetEmailUseCase(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }
}
