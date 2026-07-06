package com.example.wearzone.data.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wearzone.domain.notifications.usecase.GetRandomCouponOfferUseCase
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Periodic background job that surfaces a discount-coupon notification.
 * Respects the user's "Notifications" preference from Settings — if it's
 * off, this run is a silent no-op instead of being unscheduled entirely,
 * so re-enabling the toggle takes effect on the very next tick.
 */
@HiltWorker
class CouponReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: ISettingsRepository,
    private val getRandomCouponOfferUseCase: GetRandomCouponOfferUseCase,
    private val couponNotifier: CouponNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationsEnabled = runCatching {
            settingsRepository.observeSettingsPreferences().first().notificationsEnabled
        }.getOrDefault(true)

        if (notificationsEnabled) {
            couponNotifier.notify(getRandomCouponOfferUseCase())
        }

        return Result.success()
    }
}
