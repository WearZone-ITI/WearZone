package com.example.wearzone.data.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wearzone.domain.notifications.scheduler.ICouponNotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponNotificationSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ICouponNotificationScheduler {

    override fun start() {
        val request = PeriodicWorkRequestBuilder<CouponReminderWorker>(
            REPEAT_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
        ).build()


        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    override fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    override fun triggerNow() {
        val request = OneTimeWorkRequestBuilder<CouponReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private companion object {
        const val WORK_NAME = "coupon_reminder_work"

        const val REPEAT_INTERVAL_MINUTES = 15L
    }
}