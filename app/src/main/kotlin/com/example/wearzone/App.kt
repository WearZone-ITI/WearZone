package com.example.wearzone

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.localization.LocalizedApp
import com.example.wearzone.navigation.NavGraph
import com.example.wearzone.presentation.common.theme.AppThemeViewModel
import com.example.wearzone.presentation.common.theme.WearZoneTheme

@Composable
fun App(
    viewModel: AppThemeViewModel = hiltViewModel(),
) {
    val preferences by viewModel.settingsPreferences.collectAsState()
    val systemDarkTheme = isSystemInDarkTheme()

    val useDarkTheme = when (preferences.themeMode) {
        ThemeMode.SystemDefault -> systemDarkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    LocalizedApp(languageCode = preferences.languageCode) {
        WearZoneTheme(darkTheme = useDarkTheme) {
            NavGraph()
        }
    }
}