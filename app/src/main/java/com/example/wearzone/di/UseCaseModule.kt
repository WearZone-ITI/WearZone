package com.example.wearzone.di

import com.example.domain.auth.repository.IAuthRepository
import dagger.Module
import com.wearzone.domain.product.repository.IProductRepository
import com.wearzone.domain.product.usecase.GetProductsUseCase
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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

    @Singleton
    fun provideGetProductsUseCase(
        repository: IProductRepository
    ): GetProductsUseCase {
        return GetProductsUseCase(repository)
    }
}