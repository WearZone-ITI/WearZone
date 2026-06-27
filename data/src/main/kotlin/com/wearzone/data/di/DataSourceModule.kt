package com.wearzone.data.di

import com.wearzone.data.remote.datasource.FirebaseAuthRemoteDataSourceImpl
import com.wearzone.data.remote.datasource.IAuthRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: FirebaseAuthRemoteDataSourceImpl,
    ): IAuthRemoteDataSource
}
