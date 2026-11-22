package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val TvConsoleColorScheme =
  darkColorScheme(
    primary = ElectricTeal,
    onPrimary = OnElectricTeal,
    primaryContainer = ElectricTealContainer,
    onPrimaryContainer = TextPrimary,
    secondary = AmberGold,
    onSecondary = OnAmberGold,
    secondaryContainer = AmberGoldContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = SkyBlue,
    onTertiary = DeepNavyBg,
    tertiaryContainer = SkyBlueContainer,
    onTertiaryContainer = TextPrimary,
    background = DeepNavyBg,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder,
    error = CoralAlert,
    onError = DeepNavyBg,
    errorContainer = CoralAlertContainer,
    onErrorContainer = TextPrimary,
  )

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun TvPermGuardTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = TvConsoleColorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
