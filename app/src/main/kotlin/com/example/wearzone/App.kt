package com.example.wearzone

import android.content.res.Configuration
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.navigation.NavGraph
import com.example.wearzone.presentation.common.theme.AppThemeViewModel
import com.example.wearzone.presentation.common.theme.WearZoneTheme
import java.util.Locale

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

@Composable
private fun LocalizedApp(
    languageCode: String,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val localizedConfiguration = remember(languageCode, configuration) {
        Configuration(configuration).apply {
            val locale = Locale.forLanguageTag(languageCode)
            setLocale(locale)
            setLayoutDirection(locale)
        }
    }

    SideEffect {
        context.resources.updateConfiguration(
            localizedConfiguration,
            context.resources.displayMetrics,
        )
    }

    val layoutDirection =
        if (localizedConfiguration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfiguration,
        LocalLayoutDirection provides layoutDirection,
        content = content,
    )
}
