package com.example.domain.onboarding.usecase

import com.example.domain.onboarding.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingCompletedUseCase(
    private val repository: IAuthRepository,
) {
    operator fun invoke(): Flow<Boolean> =
        repository.observeOnboardingCompleted()
}
