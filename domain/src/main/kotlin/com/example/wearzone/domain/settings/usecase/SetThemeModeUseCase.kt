package com.example.wearzone.domain.settings.usecase

import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.domain.settings.repository.ISettingsRepository

class SetThemeModeUseCase(
    private val repository: ISettingsRepository,
) {
    suspend operator fun invoke(themeMode: ThemeMode): Result<Unit> {
        return repository.setThemeMode(themeMode)
    }
}
