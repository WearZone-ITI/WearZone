package com.example.wearzone.di

import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.onboarding.repository.IAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

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
}
