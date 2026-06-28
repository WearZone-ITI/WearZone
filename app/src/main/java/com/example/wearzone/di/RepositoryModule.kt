package com.example.wearzone.di

import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.onboarding.repository.IAuthRepository
import com.example.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import com.example.domain.onboarding.usecase.SetOnboardingCompletedUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideOnboardingRepository(
        dataSource: IOnboardingPreferencesDataSource,
        ioDispatcher: CoroutineDispatcher,
    ): IAuthRepository =
        AuthRepositoryImpl(
            dataSource = dataSource,
            ioDispatcher = ioDispatcher,
        )

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
