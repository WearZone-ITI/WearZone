package com.example.wearzone.domain.notifications.scheduler

/**
 * Schedules/cancels the recurring "coupon reminder" background work.
 * The actual mechanism (WorkManager) lives in the data layer.
 */
interface ICouponNotificationScheduler {
    fun start()
    fun stop()
}
