package com.example.dartx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DartXColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = OnGreen,
    primaryContainer = SurfaceRaised,
    onPrimaryContainer = TextPrimary,

    secondary = GreenBright,
    onSecondary = OnGreen,
    secondaryContainer = SurfaceRaised,
    onSecondaryContainer = TextPrimary,

    tertiary = GreenBright,
    onTertiary = OnGreen,
    tertiaryContainer = SurfaceRaised,
    onTertiaryContainer = TextPrimary,

    background = Ground,
    onBackground = TextPrimary,

    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextSecondary,

    surfaceContainerLowest = Ground,
    surfaceContainerLow = SurfaceRecessed,
    surfaceContainer = SurfaceCard,
    surfaceContainerHigh = SurfaceRaised,
    surfaceContainerHighest = SurfaceHigh,

    surfaceDim = Ground,
    surfaceBright = SurfaceHigh,
    inverseSurface = TextPrimary,
    inverseOnSurface = Ground,

    outline = Stroke,
    outlineVariant = StrokeFaint,

    error = Red,
    onError = OnRed,
    errorContainer = SurfaceRaised,
    onErrorContainer = RedBright,

    scrim = Ground
)

@Composable
fun DartXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DartXColorScheme,
        typography = DartXTypography,
        content = content
    )
}
