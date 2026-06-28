package com.example.wearzone.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindAuthRemoteDataSource(
        impl: com.example.data.remote.datasource.AuthRemoteDataSourceImpl
    ): com.example.data.remote.datasource.IAuthRemoteDataSource
}