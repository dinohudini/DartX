package com.dinohud.dartx.ui.screens

import androidx.compose.ui.graphics.Color

internal val avatarColorOptions = listOf(
    "#F2795E", "#E0A94A", "#5FBE8D", "#46B8CB", "#7398F2", "#B57DE3"
)

internal fun parseHexColor(hex: String): Color {
    val digits = hex.removePrefix("#")
    val argb = when (digits.length) {
        6 -> 0xFF000000 or digits.toLong(16)
        8 -> digits.toLong(16)
        else -> throw IllegalArgumentException("Unknown color: $hex")
    }
    return Color(argb.toInt())
}
