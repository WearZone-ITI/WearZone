package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.data.remote.datasource.ICategoryRemoteDataSource
import com.example.wearzone.domain.category.repository.ICategoryRepository
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: ICategoryRemoteDataSource,
    private val settingsPreferencesDataSource: ISettingsPreferencesDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICategoryRepository {

    override suspend fun getCategories(): DataResult<List<Category>> =
        withContext(ioDispatcher) {
            try {
                val languageCode = settingsPreferencesDataSource
                    .observeSettingsPreferences()
                    .first()
                    .languageCode
                val dtoList = remoteDataSource.fetchCategories()
                DataResult.Success(dtoList.map { it.toDomain(languageCode) })
            } catch (e: Exception) {
                DataResult.Error(DomainError.Unknown(e))
            }
        }
}
