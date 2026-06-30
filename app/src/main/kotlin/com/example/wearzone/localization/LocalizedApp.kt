package com.example.wearzone.localization

import android.content.res.Configuration
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

@Composable
fun LocalizedApp(
    languageCode: String,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val localizedConfiguration = remember(languageCode, configuration) {
        Configuration(configuration).apply {
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)
            setLocale(locale)
            setLayoutDirection(locale)
        }
    }

    // Update resources BEFORE composition of children so stringResource() reads the correct language instantly
    context.resources.updateConfiguration(
        localizedConfiguration,
        context.resources.displayMetrics,
    )

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