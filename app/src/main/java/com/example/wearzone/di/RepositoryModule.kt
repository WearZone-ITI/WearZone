package com.example.wearzone.di

import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.onboarding.repository.IAuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.auth.repository.IAuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl
    ): IOnboardingRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): IAuthRepository
}
