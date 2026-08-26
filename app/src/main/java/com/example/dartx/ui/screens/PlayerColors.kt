package com.example.dartx.ui.screens

import androidx.compose.ui.graphics.Color

/** Avatar colours offered when creating a player. */
internal val avatarColorOptions = listOf(
    "#F2795E", "#E0A94A", "#5FBE8D", "#46B8CB", "#7398F2", "#B57DE3"
)

internal fun parseHexColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))
