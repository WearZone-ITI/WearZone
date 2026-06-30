package com.example.wearzone.di

import com.example.wearzone.data.repository.AuthRepositoryImpl
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.data.repository.ProductRepositoryImpl
import com.example.wearzone.data.repository.SettingsRepositoryImpl
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): IProductRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): ISettingsRepository
}
