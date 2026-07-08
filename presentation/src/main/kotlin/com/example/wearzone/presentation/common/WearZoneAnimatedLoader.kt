package com.example.wearzone.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun WearZoneAnimatedLoader(
    modifier: Modifier = Modifier,
    size: Dp = 350.dp
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.wearzone_loader)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val isDark = AppTheme.colors.background == Color(0xFF000000)

    val primaryAnimColor = AppTheme.colors.selected
    val secondaryAnimColor = AppTheme.colors.textPrimary
    val bgAnimColor = if (isDark) AppTheme.colors.surface else AppTheme.colors.selected

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(primaryAnimColor.toArgb()),
            keyPath = arrayOf("bag-outside", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryAnimColor.toArgb()),
            keyPath = arrayOf("bag-inside", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(primaryAnimColor.toArgb()),
            keyPath = arrayOf("Graphs-anim", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(bgAnimColor.toArgb()),
            keyPath = arrayOf("BG", "**")
        )
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size),
        dynamicProperties = dynamicProperties
    )
}