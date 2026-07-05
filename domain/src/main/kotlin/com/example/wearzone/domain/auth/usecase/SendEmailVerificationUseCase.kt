package com.example.wearzone.domain.auth.usecase

import com.example.wearzone.domain.auth.repository.IAuthRepository

class SendEmailVerificationUseCase(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.sendEmailVerification()
    }
}
