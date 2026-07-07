package com.example.wearzone.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wearzone.domain.notifications.usecase.GetRandomCouponOfferUseCase
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.flow.first

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

    @AssistedFactory
    interface Factory {
        fun create(context: Context, params: WorkerParameters): CouponReminderWorker
    }
}