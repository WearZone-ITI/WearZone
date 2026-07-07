package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.notifications.scheduler.ICouponNotificationScheduler
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
    private val couponNotificationScheduler: ICouponNotificationScheduler,
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
                // ADDED: start/stop the periodic coupon-reminder work immediately
                // instead of waiting for the worker to notice the flag changed.
                if (enabled) {
                    couponNotificationScheduler.start()
                } else {
                    couponNotificationScheduler.stop()
                }
            }
        }


    override suspend fun setLanguage(languageCode: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                dataSource.setLanguage(languageCode)
            }
        }
}
