package com.example.wearzone.di

import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithEmailUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithGoogleUseCase
import com.example.wearzone.domain.auth.usecase.LogoutUseCase
import com.example.wearzone.domain.auth.usecase.RegisterUseCase
import com.example.wearzone.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import com.example.wearzone.domain.onboarding.usecase.SetOnboardingCompletedUseCase
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.domain.settings.usecase.SetLanguageUseCase
import com.example.wearzone.domain.settings.usecase.SetNotificationsEnabledUseCase
import com.example.wearzone.domain.settings.usecase.SetThemeModeUseCase
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
        repository: IProductRepository
    ): GetProductsUseCase {
        return GetProductsUseCase(repository)
    }

    @Provides
    fun provideGetProductDetailUseCase(
        repository: IProductRepository
    ): GetProductDetailUseCase {
        return GetProductDetailUseCase(repository)
    }

    @Provides
    fun provideLoginWithEmailUseCase(
        repository: IAuthRepository
    ): LoginWithEmailUseCase {
        return LoginWithEmailUseCase(repository)
    }

    @Provides
    fun provideLoginWithGoogleUseCase(
        repository: IAuthRepository
    ): LoginWithGoogleUseCase {
        return LoginWithGoogleUseCase(repository)
    }

    @Provides
    fun provideRegisterUseCase(
        repository: IAuthRepository
    ): RegisterUseCase {
        return RegisterUseCase(repository)
    }

    @Provides
    fun provideGetCurrentUserUseCase(
        repository: IAuthRepository
    ): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(repository)
    }

    @Provides
    fun provideLogoutUseCase(
        repository: IAuthRepository
    ): LogoutUseCase {
        return LogoutUseCase(repository)
    }

    @Provides
    fun provideObserveSettingsPreferencesUseCase(
        repository: ISettingsRepository,
    ): ObserveSettingsPreferencesUseCase {
        return ObserveSettingsPreferencesUseCase(repository)
    }

    @Provides
    fun provideSetThemeModeUseCase(
        repository: ISettingsRepository,
    ): SetThemeModeUseCase {
        return SetThemeModeUseCase(repository)
    }

    @Provides
    fun provideSetNotificationsEnabledUseCase(
        repository: ISettingsRepository,
    ): SetNotificationsEnabledUseCase {
        return SetNotificationsEnabledUseCase(repository)
    }

    @Provides
    fun provideSetLanguageUseCase(
        repository: ISettingsRepository,
    ): SetLanguageUseCase {
        return SetLanguageUseCase(repository)
    }
}

