package com.example.wearzone.domain.auth.usecase

import com.example.wearzone.domain.auth.repository.IAuthRepository

class LogoutUseCase(
    private val authRepository: IAuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
