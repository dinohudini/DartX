package com.example.dartx.ui.screens

import androidx.compose.ui.graphics.Color

/** Avatar colours offered when creating a player. */
internal val avatarColorOptions = listOf(
    "#EF5350", "#42A5F5", "#66BB6A", "#FFA726", "#AB47BC", "#26C6DA"
)

internal fun parseHexColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))
