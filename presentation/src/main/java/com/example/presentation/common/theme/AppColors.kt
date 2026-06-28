package com.example.presentation.common.theme

import androidx.compose.ui.graphics.Color
// ── Light Color Scheme (Material3) ───────────────────────────────────────────
import androidx.compose.material3.lightColorScheme

// ── Primary Palette ──────────────────────────────────────────────────────────
val MidnightSlate = Color(0xFF1A1A1B)
val ChampagneGold = Color(0xFFD4AF37)
val OffWhite      = Color(0xFFF9F9F9)

// ── Input / Surface ──────────────────────────────────────────────────────────
val screenBackground             = Color(0xFFEFEDED)
val InputBgFocused      = Color(0xFFEFEDED)
val InputBorderFocused  = MidnightSlate

// ── On-Surface ───────────────────────────────────────────────────────────────
val OnSurface           = Color(0xFF1B1C1C)
val OnSurfaceVariant    = Color(0xFF46474A)
val OutlineColor        = Color(0xFF76777B)
val OutlineVariant      = Color(0xFFC7C6CA)

// ── Feedback ─────────────────────────────────────────────────────────────────
val ErrorRed            = Color(0xFFB22222)
val SuccessGreen        = Color(0xFF4CAF50)
val WarningAmber        = Color(0xFFFFC107)
val StrengthWeak        = Color(0xFFB22222)
val StrengthFair        = Color(0xFFFF7043)
val StrengthGood        = Color(0xFFFFC107)
val StrengthStrong      = Color(0xFF66BB6A)

// ── Surface Containers ────────────────────────────────────────────────────────
val SurfaceLowest   = Color(0xFFFFFFFF)
val SurfaceLow      = Color(0xFFF5F3F3)
val SurfaceContainer= Color(0xFFEFEDED)
val SurfaceHigh     = Color(0xFFE9E8E7)
val SurfaceHighest  = Color(0xFFE3E2E2)



val WearZoneLightColors = lightColorScheme(
    primary             = MidnightSlate,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF1B1B1C),
    onPrimaryContainer  = Color(0xFF858384),
    secondary           = Color(0xFF735C00),
    onSecondary         = Color.White,
    secondaryContainer  = ChampagneGold,
    onSecondaryContainer= Color(0xFF745C00),
    tertiary            = MidnightSlate,
    onTertiary          = Color.White,
    background          = OffWhite,
    onBackground        = OnSurface,
    surface             = OffWhite,
    onSurface           = OnSurface,
    onSurfaceVariant    = OnSurfaceVariant,
    outline             = OutlineColor,
    outlineVariant      = OutlineVariant,
    error               = ErrorRed,
    onError             = Color.White,
    surfaceContainerLowest  = SurfaceLowest,
    surfaceContainerLow     = SurfaceLow,
    surfaceContainer        = SurfaceContainer,
    surfaceContainerHigh    = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
)
