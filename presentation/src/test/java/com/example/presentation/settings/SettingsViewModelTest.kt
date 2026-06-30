package com.example.presentation.settings

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.domain.settings.usecase.SetLanguageUseCase
import com.example.wearzone.domain.settings.usecase.SetNotificationsEnabledUseCase
import com.example.wearzone.domain.settings.usecase.SetThemeModeUseCase
import com.example.wearzone.presentation.settings.SettingsUiEffect
import com.example.wearzone.presentation.settings.SettingsUiIntent
import com.example.wearzone.presentation.settings.SettingsUiState
import com.example.wearzone.presentation.settings.SettingsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load shows settings content`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value as SettingsUiState.Content

        assertEquals(ThemeMode.SystemDefault, state.selectedThemeMode)
        assertEquals(true, state.notificationsEnabled)
    }

    @Test
    fun `theme mode selection updates repository state`() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository)

        viewModel.handleIntent(SettingsUiIntent.OnThemeModeSelected(ThemeMode.Dark))

        assertEquals(ThemeMode.Dark, repository.preferences.value.themeMode)
    }

    @Test
    fun `notifications toggle updates repository state`() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository)

        viewModel.handleIntent(SettingsUiIntent.OnNotificationsToggled(false))

        assertEquals(false, repository.preferences.value.notificationsEnabled)
    }

    @Test
    fun `language click emits picker effect`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(SettingsUiIntent.OnLanguageClicked)

            assertEquals(SettingsUiEffect.ShowLanguagePicker, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `language selection updates repository state`() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository)

        viewModel.uiEffect.test {
            viewModel.handleIntent(SettingsUiIntent.OnLanguageSelected("ar"))

            assertEquals("ar", repository.preferences.value.languageCode)
            assertEquals(SettingsUiEffect.ShowLanguageUpdated, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `privacy click emits privacy policy effect`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(SettingsUiIntent.OnPrivacyPolicyClicked)

            assertEquals(SettingsUiEffect.ShowPrivacyPolicy, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `terms click emits terms effect`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(SettingsUiIntent.OnTermsClicked)

            assertEquals(SettingsUiEffect.ShowTerms, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        repository: FakeSettingsRepository = FakeSettingsRepository(),
    ): SettingsViewModel =
        SettingsViewModel(
            observeSettingsPreferencesUseCase = ObserveSettingsPreferencesUseCase(repository),
            setThemeModeUseCase = SetThemeModeUseCase(repository),
            setNotificationsEnabledUseCase = SetNotificationsEnabledUseCase(repository),
            setLanguageUseCase = SetLanguageUseCase(repository),
        )

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
