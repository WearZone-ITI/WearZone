package com.example.wearzone.domain.settings.usecase

import com.example.wearzone.domain.settings.repository.ISettingsRepository

class ObserveSettingsPreferencesUseCase(
    private val repository: ISettingsRepository,
) {
    operator fun invoke() = repository.observeSettingsPreferences()
}
