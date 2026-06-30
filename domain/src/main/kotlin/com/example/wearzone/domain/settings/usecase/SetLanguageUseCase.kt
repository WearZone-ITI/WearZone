package com.example.wearzone.domain.settings.usecase

import com.example.wearzone.domain.settings.repository.ISettingsRepository

class SetLanguageUseCase(
    private val repository: ISettingsRepository,
) {
    suspend operator fun invoke(languageCode: String): Result<Unit> {
        return repository.setLanguage(languageCode)
    }
}
