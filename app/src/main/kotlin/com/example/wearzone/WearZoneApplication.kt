package com.example.wearzone

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class WearZoneApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingsRepository: ISettingsRepository

    @Inject
    lateinit var couponNotificationScheduler: ICouponNotificationScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleCouponRemindersIfEnabled()
    }

    private fun scheduleCouponRemindersIfEnabled() {
        applicationScope.launch {
            val notificationsEnabled = runCatching {
                settingsRepository.observeSettingsPreferences().first().notificationsEnabled
            }.getOrDefault(true)

            if (notificationsEnabled) {
                couponNotificationScheduler.start()
            }
        }
    }
}
