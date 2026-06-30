package com.example.wearzone.di

import com.example.wearzone.data.repository.AuthRepositoryImpl
import com.example.wearzone.data.repository.ProductRepositoryImpl
import com.example.wearzone.data.repository.search.RecentSearchRepositoryImpl
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.search.repository.IRecentSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): IProductRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds
    abstract fun bindRecentSearchRepository(impl: RecentSearchRepositoryImpl): IRecentSearchRepository
}
