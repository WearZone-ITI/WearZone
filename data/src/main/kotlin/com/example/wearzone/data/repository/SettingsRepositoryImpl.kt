package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataSource: ISettingsPreferencesDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ISettingsRepository {

    override fun observeSettingsPreferences(): Flow<SettingsPreferences> =
        dataSource.observeSettingsPreferences().flowOn(ioDispatcher)

    override suspend fun setThemeMode(themeMode: ThemeMode): Result<Unit> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                dataSource.setThemeMode(themeMode)
            }
        }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                dataSource.setNotificationsEnabled(enabled)
            }
        }

    override suspend fun setLanguage(languageCode: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                dataSource.setLanguage(languageCode)
            }
        }

    override suspend fun setCurrency(currencyCode: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                dataSource.setCurrency(currencyCode)
            }
        }
}
