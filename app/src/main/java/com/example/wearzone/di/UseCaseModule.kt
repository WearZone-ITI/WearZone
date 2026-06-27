package com.example.wearzone.di

import com.example.domain.auth.repository.IAuthRepository
import com.example.domain.auth.usecase.LoginWithEmailUseCase
import com.example.domain.auth.usecase.LoginWithGoogleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideLoginWithEmailUseCase(
        authRepository: IAuthRepository
    ): LoginWithEmailUseCase {
        return LoginWithEmailUseCase(authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideLoginWithGoogleUseCase(
        authRepository: IAuthRepository
    ): LoginWithGoogleUseCase {
        return LoginWithGoogleUseCase(authRepository)
    }
}
