package com.example.wearzone

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.wearzone.data.notifications.WearZoneWorkerFactory
import com.example.wearzone.domain.notifications.scheduler.ICouponNotificationScheduler
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltAndroidApp
class WearZoneApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var settingsRepository: ISettingsRepository

    @Inject
    lateinit var couponNotificationScheduler: ICouponNotificationScheduler

    @Inject
    lateinit var wearZoneWorkerFactory: WearZoneWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(wearZoneWorkerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        try {
            WorkManager.initialize(this, workManagerConfiguration)
        } catch (e: IllegalStateException) {}

        scheduleCouponRemindersIfEnabled()
    }

    private fun scheduleCouponRemindersIfEnabled() {
        applicationScope.launch {
            kotlinx.coroutines.delay(1000.milliseconds)

            val notificationsEnabled = runCatching {
                settingsRepository.observeSettingsPreferences().first().notificationsEnabled
            }.getOrDefault(true)

            if (notificationsEnabled) {
                couponNotificationScheduler.start()
            }
        }
    }
}