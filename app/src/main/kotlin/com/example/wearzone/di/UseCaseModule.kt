package com.example.wearzone.di

import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithEmailUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithGoogleUseCase
import com.example.wearzone.domain.auth.usecase.RegisterUseCase
import com.example.wearzone.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import com.example.wearzone.domain.onboarding.usecase.SetOnboardingCompletedUseCase
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.product.usecase.SearchProductsUseCase
import com.example.wearzone.domain.search.repository.IRecentSearchRepository
import com.example.wearzone.domain.search.usecase.ClearRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.GetRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.SaveRecentSearchUseCase
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
    ): ObserveOnboardingCompletedUseCase = ObserveOnboardingCompletedUseCase(repository)

    @Provides
    fun provideSetOnboardingCompletedUseCase(
        repository: IAuthRepository,
    ): SetOnboardingCompletedUseCase = SetOnboardingCompletedUseCase(repository)

    @Provides
    fun provideGetProductsUseCase(
        repository: IProductRepository,
    ): GetProductsUseCase = GetProductsUseCase(repository)

    @Provides
    fun provideSearchProductsUseCase(
        repository: IProductRepository,
    ): SearchProductsUseCase = SearchProductsUseCase(repository)

    @Provides
    fun provideGetRecentSearchesUseCase(
        repository: IRecentSearchRepository,
    ): GetRecentSearchesUseCase = GetRecentSearchesUseCase(repository)

    @Provides
    fun provideSaveRecentSearchUseCase(
        repository: IRecentSearchRepository,
    ): SaveRecentSearchUseCase = SaveRecentSearchUseCase(repository)

    @Provides
    fun provideClearRecentSearchesUseCase(
        repository: IRecentSearchRepository,
    ): ClearRecentSearchesUseCase = ClearRecentSearchesUseCase(repository)

    @Provides
    fun provideLoginWithEmailUseCase(
        repository: IAuthRepository,
    ): LoginWithEmailUseCase = LoginWithEmailUseCase(repository)

    @Provides
    fun provideLoginWithGoogleUseCase(
        repository: IAuthRepository,
    ): LoginWithGoogleUseCase = LoginWithGoogleUseCase(repository)

    @Provides
    fun provideRegisterUseCase(
        repository: IAuthRepository,
    ): RegisterUseCase = RegisterUseCase(repository)

    @Provides
    fun provideGetCurrentUserUseCase(
        repository: IAuthRepository,
    ): GetCurrentUserUseCase = GetCurrentUserUseCase(repository)
}
