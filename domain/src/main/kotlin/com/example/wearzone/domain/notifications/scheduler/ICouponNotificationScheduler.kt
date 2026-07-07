package com.example.wearzone.domain.notifications.scheduler

/**
 * Schedules/cancels the recurring "coupon reminder" background work.
 * The actual mechanism (WorkManager) lives in the data layer.
 */
interface ICouponNotificationScheduler {
    fun start()
    fun stop()

    /**
     * Runs the coupon-reminder check once, immediately, without touching the
     * periodic schedule. Used right after the user grants the notification
     * permission, since the periodic job's first tick may well have already
     * run (and silently skipped) before the permission dialog was answered.
     */
    fun triggerNow()
}