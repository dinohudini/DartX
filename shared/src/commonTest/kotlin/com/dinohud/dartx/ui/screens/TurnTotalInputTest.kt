package com.dinohud.dartx.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class TurnTotalInputTest {

    @Test
    fun `zero can be typed as a whole turn`() {
        assertEquals("0", appendDigit("", "0"))
    }

    @Test
    fun `a digit after a lone zero replaces it`() {
        assertEquals("5", appendDigit("0", "5"))
        assertEquals("0", appendDigit("0", "0"))
    }

    @Test
    fun `zeros inside a number are kept`() {
        assertEquals("10", appendDigit("1", "0"))
        assertEquals("100", appendDigit("10", "0"))
    }

    @Test
    fun `input stops at three digits`() {
        assertEquals("140", appendDigit("140", "5"))
    }
}
