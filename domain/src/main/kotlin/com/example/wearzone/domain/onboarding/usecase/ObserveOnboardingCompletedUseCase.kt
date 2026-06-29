package com.example.wearzone.domain.onboarding.usecase

import com.example.wearzone.domain.auth.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingCompletedUseCase(
    private val repository: IAuthRepository,
) {
    operator fun invoke(): Flow<Boolean> =
        repository.observeOnboardingCompleted()
}