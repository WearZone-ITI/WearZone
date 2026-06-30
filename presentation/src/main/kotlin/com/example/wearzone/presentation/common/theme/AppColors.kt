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
    background = Color(0xFF161316),
    surface = Color(0xFF1C181D),
    surfaceVariant = Color(0xFF211D22),
    card = Color(0xFF211D22),
    textPrimary = Color(0xFFF4EEF1),
    textSecondary = Color(0xFFCFC3CA),
    divider = Color(0xFF473F45),
    border = Color(0xFF91858C),
    selected = Color(0xFFD7A7B4),
    accent = Color(0xFFD7A7B4),
    error = Color(0xFFFFB4AB),
    success = Color(0xFF8BD49C),
    warning = Color(0xFFF2C66D),
    onAccent = Color(0xFF2B1720),
    scrim = Color(0xCC0B090C),
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
    primary = Color(0xFFD7A7B4),
    onPrimary = Color(0xFF2B1720),
    primaryContainer = Color(0xFF4A2E39),
    onPrimaryContainer = Color(0xFFFFD9E3),

    secondary = Color(0xFFCBB9C1),
    onSecondary = Color(0xFF241A20),
    secondaryContainer = Color(0xFF3B2D34),
    onSecondaryContainer = Color(0xFFEBDDE3),

    tertiary = Color(0xFFB7C4D3),
    onTertiary = Color(0xFF18212B),
    tertiaryContainer = Color(0xFF2D3743),
    onTertiaryContainer = Color(0xFFD7E4F4),

    background = Color(0xFF161316),
    onBackground = Color(0xFFF4EEF1),

    surface = Color(0xFF1C181D),
    onSurface = Color(0xFFF4EEF1),

    surfaceVariant = Color(0xFF3A3338),
    onSurfaceVariant = Color(0xFFCFC3CA),

    outline = Color(0xFF91858C),
    outlineVariant = Color(0xFF473F45),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    surfaceContainerLowest = Color(0xFF110F12),
    surfaceContainerLow = Color(0xFF1A171B),
    surfaceContainer = Color(0xFF211D22),
    surfaceContainerHigh = Color(0xFF2B262C),
    surfaceContainerHighest = Color(0xFF352F36),

    inverseSurface = Color(0xFFE8DDE3),
    inverseOnSurface = Color(0xFF332D32),
    inversePrimary = Color(0xFF755062),
    scrim = Color(0xCC0B090C),
)
