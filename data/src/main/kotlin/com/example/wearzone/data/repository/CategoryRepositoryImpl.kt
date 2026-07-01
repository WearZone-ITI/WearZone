package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.datasource.ICategoryRemoteDataSource
import com.example.wearzone.domain.category.repository.ICategoryRepository
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: ICategoryRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICategoryRepository {

    override suspend fun getCategories(): DataResult<List<Category>> =
        withContext(ioDispatcher) {
            try {
                val dtoList = remoteDataSource.fetchCategories()
                DataResult.Success(dtoList.map { it.toDomain() })
            } catch (e: Exception) {
                DataResult.Error(DomainError.Unknown(e))
            }
        }
}