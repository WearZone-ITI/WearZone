package com.example.wearzone.di

import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.auth.repository.IAuthRepository
import com.wearzone.data.repository.ProductRepositoryImpl
import com.wearzone.domain.product.repository.IProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): IProductRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): IAuthRepository
}
