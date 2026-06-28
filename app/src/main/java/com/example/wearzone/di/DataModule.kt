package com.example.wearzone.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.data.local.datastore.OnboardingPreferencesDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideOnboardingPreferencesDataSource(
        dataStore: DataStore<Preferences>,
    ): IOnboardingPreferencesDataSource =
        OnboardingPreferencesDataSourceImpl(dataStore)
}
