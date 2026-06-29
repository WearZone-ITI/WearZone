package com.example.wearzone.di

import com.wearzone.domain.product.repository.IProductRepository
import com.wearzone.domain.product.usecase.GetProductsUseCase
import dagger.Provides
import javax.inject.Singleton

object UseCaseModule {
    @Provides
    @Singleton
    fun provideGetProductsUseCase(
        repository: IProductRepository
    ): GetProductsUseCase {
        return GetProductsUseCase(repository)
    }
}