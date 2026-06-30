package com.example.wearzone.presentation.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object AppTheme {
    val colors: WearZoneColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWearZoneColors.current
}

@Composable
fun WearZoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WearZoneDarkColors else WearZoneLightColors
    val appColors = if (darkTheme) WearZoneDarkAppColors else WearZoneLightAppColors

    CompositionLocalProvider(LocalWearZoneColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = WearZoneShapes,
            content = content
        )
    }
}
