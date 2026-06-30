package com.example.wearzone.domain.settings.usecase

import com.example.wearzone.domain.settings.repository.ISettingsRepository

class SetNotificationsEnabledUseCase(
    private val repository: ISettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> {
        return repository.setNotificationsEnabled(enabled)
    }
}
