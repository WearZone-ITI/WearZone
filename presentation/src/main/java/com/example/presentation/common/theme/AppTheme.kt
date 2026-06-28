package com.example.presentation.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun WearZoneTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WearZoneLightColors,
        typography  = WearZoneTypography,
        shapes      = WearZoneShapes,
        content     = content,
    )
}
