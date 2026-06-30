package com.example.domain.settings.usecase

import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.domain.settings.usecase.SetLanguageUseCase
import com.example.wearzone.domain.settings.usecase.SetNotificationsEnabledUseCase
import com.example.wearzone.domain.settings.usecase.SetThemeModeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUseCasesTest {

    private val repository = FakeSettingsRepository()

    @Test
    fun `observe settings returns repository flow`() = runTest {
        val useCase = ObserveSettingsPreferencesUseCase(repository)

        assertEquals(repository.preferences, useCase())
    }

    @Test
    fun `set theme mode delegates to repository`() = runTest {
        val useCase = SetThemeModeUseCase(repository)

        val result = useCase(ThemeMode.Dark)

        assertTrue(result.isSuccess)
        assertEquals(ThemeMode.Dark, repository.preferences.value.themeMode)
    }

    @Test
    fun `set notifications delegates to repository`() = runTest {
        val useCase = SetNotificationsEnabledUseCase(repository)

        val result = useCase(false)

        assertTrue(result.isSuccess)
        assertEquals(false, repository.preferences.value.notificationsEnabled)
    }

    @Test
    fun `set language delegates to repository`() = runTest {
        val useCase = SetLanguageUseCase(repository)

        val result = useCase("ar")

        assertTrue(result.isSuccess)
        assertEquals("ar", repository.preferences.value.languageCode)
    }

    private class FakeSettingsRepository : ISettingsRepository {
        val preferences = MutableStateFlow(SettingsPreferences())

        override fun observeSettingsPreferences(): Flow<SettingsPreferences> = preferences

        override suspend fun setThemeMode(themeMode: ThemeMode): Result<Unit> {
            preferences.value = preferences.value.copy(themeMode = themeMode)
            return Result.success(Unit)
        }

        override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
            preferences.value = preferences.value.copy(notificationsEnabled = enabled)
            return Result.success(Unit)
        }

        override suspend fun setLanguage(languageCode: String): Result<Unit> {
            preferences.value = preferences.value.copy(languageCode = languageCode)
            return Result.success(Unit)
        }
    }
}
