package com.dinohud.dartx.ui.screens

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlayerColorsTest {

    @Test
    fun rgbHex_isOpaque() {
        assertEquals(Color(0xFFF2795E), parseHexColor("#F2795E"))
    }

    @Test
    fun argbHex_keepsAlpha() {
        assertEquals(Color(0x80123456), parseHexColor("#80123456"))
    }

    @Test
    fun everyAvatarOption_parses() {
        avatarColorOptions.forEach { parseHexColor(it) }
    }

    @Test
    fun malformedHex_fails() {
        assertFailsWith<IllegalArgumentException> { parseHexColor("#123") }
    }
}
