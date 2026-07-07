package com.example.wearzone.presentation.common.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val MidnightSlate = Color(0xFF1A1A1B)
val ChampagneGold = Color(0xFFD4AF37)
val OffWhite = Color(0xFFF9F9F9)

val screenBackground = Color(0xFFEFEDED)
val InputBgFocused = Color(0xFFEFEDED)
val InputBorderFocused = MidnightSlate

val OnSurface = Color(0xFF1B1C1C)
val OnSurfaceVariant = Color(0xFF46474A)
val OutlineColor = Color(0xFF76777B)
val OutlineVariant = Color(0xFFC7C6CA)

object AppColors {
    val Primary = Color(0xFF1A1A1B)
    val Background = Color(0xFFFBF9F9)
    val InputBackground = Color(0xFFFFFFFF)
    val OnPrimary = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF76777B)
    val InputBorder = Color(0xFF6B7280)
    val Divider = Color(0xFFE9E8E7)
    val Error = Color(0xFFFF3B30)
    val Warning = Color(0xFFEAB308)
    val Success = Color(0xFF4CAF50)
}

val ErrorRed = Color(0xFFB22222)
val SuccessGreen = Color(0xFF4CAF50)
val WarningAmber = Color(0xFFFFC107)
val StrengthWeak = Color(0xFFB22222)
val StrengthFair = Color(0xFFFF7043)
val StrengthGood = Color(0xFFFFC107)
val StrengthStrong = Color(0xFF66BB6A)

val SurfaceLowest = Color(0xFFFFFFFF)
val SurfaceLow = Color(0xFFF5F3F3)
val SurfaceContainer = Color(0xFFEFEDED)
val SurfaceHigh = Color(0xFFE9E8E7)
val SurfaceHighest = Color(0xFFE3E2E2)

@Immutable
data class WearZoneColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val border: Color,
    val selected: Color,
    val accent: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
    val onAccent: Color,
    val scrim: Color,
)

val WearZoneLightAppColors = WearZoneColors(
    background = AppColors.Background,
    surface = AppColors.InputBackground,
    surfaceVariant = SurfaceLow,
    card = AppColors.InputBackground,
    textPrimary = AppColors.TextPrimary,
    textSecondary = AppColors.TextSecondary,
    divider = AppColors.Divider,
    border = AppColors.InputBorder,
    selected = AppColors.Primary,
    accent = ChampagneGold,
    error = AppColors.Error,
    success = AppColors.Success,
    warning = AppColors.Warning,
    onAccent = AppColors.OnPrimary,
    scrim = Color.Black.copy(alpha = 0.7f),
)

val WearZoneDarkAppColors = WearZoneColors(
    background = Color(0xFF000000),
    surface = Color(0xFF141414),
    surfaceVariant = Color(0xFF1F1F1F),
    card = Color(0xFF141414),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0B0B0),
    divider = Color(0xFF222222),
    border = Color(0xFF333333),
    selected = ChampagneGold,
    accent = ChampagneGold,
    error = Color(0xFFFFB4AB),
    success = Color(0xFF8BD49C),
    warning = Color(0xFFF2C66D),
    onAccent = Color(0xFF000000),
    scrim = Color(0xCC000000),
)

val LocalWearZoneColors = staticCompositionLocalOf { WearZoneLightAppColors }

val WearZoneLightColors = lightColorScheme(
    primary = MidnightSlate,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B1B1C),
    onPrimaryContainer = Color(0xFF858384),
    secondary = Color(0xFF735C00),
    onSecondary = Color.White,
    secondaryContainer = ChampagneGold,
    onSecondaryContainer = Color(0xFF745C00),
    tertiary = MidnightSlate,
    onTertiary = Color.White,
    background = OffWhite,
    onBackground = OnSurface,
    surface = OffWhite,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineColor,
    outlineVariant = OutlineVariant,
    error = ErrorRed,
    onError = Color.White,
    surfaceContainerLowest = SurfaceLowest,
    surfaceContainerLow = SurfaceLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
)

val WearZoneDarkColors = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2D240A),
    onPrimaryContainer = ChampagneGold,

    secondary = ChampagneGold,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF141414),
    onSecondaryContainer = Color(0xFFFFFFFF),

    tertiary = ChampagneGold,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF1F1F1F),
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),

    surface = Color(0xFF141414),
    onSurface = Color(0xFFFFFFFF),

    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFFB0B0B0),

    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF222222),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = Color(0xFF0F0F0F),
    surfaceContainer = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF1F1F1F),
    surfaceContainerHighest = Color(0xFF2D2D2D),

    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF141414),
    inversePrimary = Color(0xFF735C00),
    scrim = Color(0xCC000000),
)
