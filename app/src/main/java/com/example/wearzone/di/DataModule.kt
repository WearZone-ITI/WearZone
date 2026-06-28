package com.example.wearzone.di

import com.example.data.db.datasource.AuthRemoteDataSourceImpl
import com.example.data.db.datasource.IAuthRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl
    ): IAuthRemoteDataSource
}