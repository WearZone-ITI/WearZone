package com.example.wearzone.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.wearzone.presentation.common.theme.ChampagneGold

@Composable
fun WearZoneAnimatedLoader(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.wearzone_loader)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val goldColor = ChampagneGold
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val secondaryColor = onBackgroundColor.copy(alpha = 0.6f)
    val backgroundTint = onBackgroundColor.copy(alpha = 0.05f)

    val dynamicProperties = rememberLottieDynamicProperties(
        // Primary shapes (bag-outside) -> Gold
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(goldColor.toArgb()),
            keyPath = arrayOf("bag-outside", "**")
        ),
        // Secondary shapes (bag-inside / items) -> adaptive onBackground
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryColor.toArgb()),
            keyPath = arrayOf("bag-inside", "**")
        ),
        // Sparks/Graphs animations -> Gold
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(goldColor.toArgb()),
            keyPath = arrayOf("Graphs-anim", "**")
        ),
        // BG (the background circle/ring) -> adaptive faint background
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(backgroundTint.toArgb()),
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
