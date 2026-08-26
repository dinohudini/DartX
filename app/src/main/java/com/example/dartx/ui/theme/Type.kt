package com.example.dartx.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.dartx.R

val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold)
)

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold)
)

private const val TABULAR = "tnum"

private fun score(size: Int, height: Int) = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.Bold,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = TABULAR
)

private fun ui(weight: FontWeight, size: Int, height: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = Barlow,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = tracking.sp
)

val DartXTypography = Typography(
    displayLarge = score(56, 56),
    displayMedium = score(44, 44),
    displaySmall = score(34, 36),

    headlineLarge = ui(FontWeight.SemiBold, 30, 36),
    headlineMedium = ui(FontWeight.SemiBold, 26, 32),
    headlineSmall = ui(FontWeight.SemiBold, 22, 28),

    titleLarge = ui(FontWeight.SemiBold, 20, 26),
    titleMedium = ui(FontWeight.SemiBold, 17, 22),
    titleSmall = ui(FontWeight.SemiBold, 15, 20),

    bodyLarge = ui(FontWeight.Normal, 16, 24),
    bodyMedium = ui(FontWeight.Normal, 15, 22),
    bodySmall = ui(FontWeight.Normal, 13, 18),

    labelLarge = ui(FontWeight.SemiBold, 15, 20, 0.6),
    labelMedium = ui(FontWeight.SemiBold, 11, 14, 1.0),
    labelSmall = ui(FontWeight.SemiBold, 11, 14, 1.0)
)
