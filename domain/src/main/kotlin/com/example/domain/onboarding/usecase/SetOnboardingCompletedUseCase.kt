package com.example.domain.onboarding.usecase

import com.example.domain.auth.repository.IAuthRepository

class SetOnboardingCompletedUseCase(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(completed: Boolean): Result<Unit> =
        repository.setOnboardingCompleted(completed)
}
