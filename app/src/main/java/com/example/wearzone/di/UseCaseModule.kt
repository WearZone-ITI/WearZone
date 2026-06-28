package com.example.wearzone.di

import com.example.domain.auth.repository.IAuthRepository
import com.example.domain.auth.usecase.LoginWithEmailUseCase
import com.example.domain.auth.usecase.LoginWithGoogleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginWithEmailUseCase(
        authRepository: IAuthRepository
    ): LoginWithEmailUseCase {
        return LoginWithEmailUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideLoginWithGoogleUseCase(
        authRepository: IAuthRepository
    ): LoginWithGoogleUseCase {
        return LoginWithGoogleUseCase(authRepository)
    }
}
