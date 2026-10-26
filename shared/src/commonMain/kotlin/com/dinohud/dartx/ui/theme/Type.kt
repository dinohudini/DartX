package com.dinohud.dartx.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.barlow_bold
import com.dinohud.dartx.resources.barlow_condensed_bold
import com.dinohud.dartx.resources.barlow_condensed_semibold
import com.dinohud.dartx.resources.barlow_medium
import com.dinohud.dartx.resources.barlow_regular
import com.dinohud.dartx.resources.barlow_semibold
import org.jetbrains.compose.resources.Font

@Composable
private fun barlow() = FontFamily(
    Font(Res.font.barlow_regular, FontWeight.Normal),
    Font(Res.font.barlow_medium, FontWeight.Medium),
    Font(Res.font.barlow_semibold, FontWeight.SemiBold),
    Font(Res.font.barlow_bold, FontWeight.Bold)
)

@Composable
private fun barlowCondensed() = FontFamily(
    Font(Res.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(Res.font.barlow_condensed_bold, FontWeight.Bold)
)

private const val TABULAR = "tnum"

private fun score(family: FontFamily, size: Int, height: Int) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Bold,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = TABULAR
)

private fun ui(family: FontFamily, weight: FontWeight, size: Int, height: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = tracking.sp
)

@Composable
fun dartXTypography(): Typography {
    val barlow = barlow()
    val condensed = barlowCondensed()
    return Typography(
        displayLarge = score(condensed, 56, 56),
        displayMedium = score(condensed, 44, 44),
        displaySmall = score(condensed, 34, 36),

        headlineLarge = ui(barlow, FontWeight.SemiBold, 30, 36),
        headlineMedium = ui(barlow, FontWeight.SemiBold, 26, 32),
        headlineSmall = ui(barlow, FontWeight.SemiBold, 22, 28),

        titleLarge = ui(barlow, FontWeight.SemiBold, 20, 26),
        titleMedium = ui(barlow, FontWeight.SemiBold, 17, 22),
        titleSmall = ui(barlow, FontWeight.SemiBold, 15, 20),

        bodyLarge = ui(barlow, FontWeight.Normal, 16, 24),
        bodyMedium = ui(barlow, FontWeight.Normal, 15, 22),
        bodySmall = ui(barlow, FontWeight.Normal, 13, 18),

        labelLarge = ui(barlow, FontWeight.SemiBold, 15, 20, 0.6),
        labelMedium = ui(barlow, FontWeight.SemiBold, 11, 14, 1.0),
        labelSmall = ui(barlow, FontWeight.SemiBold, 11, 14, 1.0)
    )
}
