package com.example.wearzone.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.data.local.datastore.OnboardingPreferencesDataSourceImpl
import com.example.data.db.datasource.AuthRemoteDataSourceImpl
import com.example.data.db.datasource.IAuthRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl
    ): IAuthRemoteDataSource

    companion object {

        @Provides
        @Singleton
        fun provideOnboardingPreferencesDataSource(
            dataStore: DataStore<Preferences>,
        ): IOnboardingPreferencesDataSource {
            return OnboardingPreferencesDataSourceImpl(dataStore)
        }
    }
}
