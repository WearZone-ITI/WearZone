package com.example.wearzone.data.notifications // 👈 ركز جداً في الـ package name دي

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

class WearZoneWorkerFactory @Inject constructor(
    private val couponWorkerFactory: CouponReminderWorker.Factory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CouponReminderWorker::class.java.name -> {
                couponWorkerFactory.create(appContext, workerParameters)
            }
            else -> null
        }
    }
}