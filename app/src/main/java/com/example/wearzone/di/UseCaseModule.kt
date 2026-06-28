package com.example.wearzone.di

import com.example.domain.onboarding.repository.IAuthRepository
import com.example.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import com.example.domain.onboarding.usecase.SetOnboardingCompletedUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideObserveOnboardingCompletedUseCase(
        repository: IAuthRepository,
    ): ObserveOnboardingCompletedUseCase =
        ObserveOnboardingCompletedUseCase(repository)

    @Provides
    fun provideSetOnboardingCompletedUseCase(
        repository: IAuthRepository,
    ): SetOnboardingCompletedUseCase =
        SetOnboardingCompletedUseCase(repository)
}
