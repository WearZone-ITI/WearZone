package com.example.wearzone.domain.auth.usecase

import com.example.wearzone.domain.auth.repository.IAuthRepository

class CheckEmailVerifiedUseCase(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return authRepository.checkEmailVerified()
    }
}
