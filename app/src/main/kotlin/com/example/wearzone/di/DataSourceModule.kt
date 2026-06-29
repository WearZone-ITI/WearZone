package com.example.wearzone.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.wearzone.data.remote.datasource.AuthRemoteDataSourceImpl
import com.example.wearzone.data.remote.datasource.IAuthRemoteDataSource
import com.example.wearzone.data.local.datasource.IOnboardingPreferencesDataSource
import com.example.wearzone.data.local.datasource.OnboardingPreferencesDataSourceImpl
import com.example.wearzone.data.remote.datasource.IProductRemoteDataSource
import com.example.wearzone.data.remote.datasource.ProductRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "onboarding_preferences",
)

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindProductRemoteDataSource(productRemoteDataSourceImpl: ProductRemoteDataSourceImpl): IProductRemoteDataSource
    @Binds
    abstract fun bindAuthRemoteDataSource(impl: AuthRemoteDataSourceImpl): IAuthRemoteDataSource

    companion object {
        @Provides
        fun provideOnboardingPreferencesDataSource(dataStore: DataStore<Preferences>): IOnboardingPreferencesDataSource {
            return OnboardingPreferencesDataSourceImpl(dataStore)
        }

        @Provides
        fun provideOnboardingDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.onboardingDataStore
    }
}
