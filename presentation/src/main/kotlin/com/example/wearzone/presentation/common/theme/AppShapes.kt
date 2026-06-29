package com.example.wearzone.presentation.common.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val WearZoneShapes = Shapes(
    extraSmall  = RoundedCornerShape(4.dp),  // chips, badges
    small       = RoundedCornerShape(8.dp),  // inputs
    medium      = RoundedCornerShape(18.dp), // buttons (pill-lite)
    large       = RoundedCornerShape(20.dp), // product cards
    extraLarge  = RoundedCornerShape(28.dp), // bottom sheets (top-only in usage)
)
