package com.example.wearzone.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.wearzone.data.local.datasource.CartLocalDataSourceImpl
import com.example.wearzone.data.local.datasource.ICartLocalDataSource
import com.example.wearzone.data.local.datasource.IOnboardingPreferencesDataSource
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.data.local.datasource.OnboardingPreferencesDataSourceImpl
import com.example.wearzone.data.local.datasource.SettingsPreferencesDataSourceImpl
import com.example.wearzone.data.remote.datasource.AuthRemoteDataSourceImpl
import com.example.wearzone.data.remote.datasource.CartRemoteDataSourceImpl
import com.example.wearzone.data.remote.datasource.CategoryRemoteDataSourceImpl
import com.example.wearzone.data.remote.datasource.CustomerAddressRemoteDataSourceImpl
import com.example.wearzone.data.remote.datasource.IAuthRemoteDataSource
import com.example.wearzone.data.remote.datasource.ICartRemoteDataSource
import com.example.wearzone.data.remote.datasource.ICustomerAddressRemoteDataSource
import com.example.wearzone.data.remote.datasource.ICategoryRemoteDataSource
import com.example.wearzone.data.remote.datasource.IOrderRemoteDataSource
import com.example.wearzone.data.remote.datasource.IProductRemoteDataSource
import com.example.wearzone.data.remote.datasource.OrderRemoteDataSourceImpl
import com.example.wearzone.data.remote.datasource.ProductRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OnboardingDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsDataStore

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "onboarding_preferences",
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_preferences",
)

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindProductRemoteDataSource(
        productRemoteDataSourceImpl: ProductRemoteDataSourceImpl,
    ): IProductRemoteDataSource

    @Binds
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl,
    ): IAuthRemoteDataSource

    @Binds
    abstract fun bindCartLocalDataSource(
        impl: CartLocalDataSourceImpl,
    ): ICartLocalDataSource

    @Binds
    abstract fun bindCartRemoteDataSource(
        impl: CartRemoteDataSourceImpl,
    ): ICartRemoteDataSource

    @Binds
    abstract fun bindCategoryRemoteDataSource(
        categoryRemoteDataSourceImpl: CategoryRemoteDataSourceImpl,
    ): ICategoryRemoteDataSource

    @Binds
    abstract fun bindCustomerAddressRemoteDataSource(
        impl: CustomerAddressRemoteDataSourceImpl,
    ): ICustomerAddressRemoteDataSource

    @Binds
    abstract fun bindOrderRemoteDataSource(
        impl: OrderRemoteDataSourceImpl,
    ): IOrderRemoteDataSource

    companion object {

        @Provides
        @OnboardingDataStore
        fun provideOnboardingDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            context.onboardingDataStore

        @Provides
        fun provideOnboardingPreferencesDataSource(
            @OnboardingDataStore dataStore: DataStore<Preferences>,
        ): IOnboardingPreferencesDataSource {
            return OnboardingPreferencesDataSourceImpl(dataStore)
        }

        @Provides
        @SettingsDataStore
        fun provideSettingsDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            context.settingsDataStore

        @Provides
        fun provideSettingsPreferencesDataSource(
            @SettingsDataStore dataStore: DataStore<Preferences>,
        ): ISettingsPreferencesDataSource {
            return SettingsPreferencesDataSourceImpl(dataStore)
        }
    }
}
