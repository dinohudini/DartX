package com.dinohud.dartx.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatTest {

    @Test
    fun padsMissingDecimals() {
        assertEquals("60.00", 60.0.toFixed(2))
        assertEquals("80.5", 80.5.toFixed(1))
    }

    @Test
    fun roundsHalfUp() {
        assertEquals("26.67", (80.0 / 3).toFixed(2))
        assertEquals("0.13", 0.125.toFixed(2))
    }

    @Test
    fun keepsLeadingZeroInFraction() {
        assertEquals("1.05", 1.05.toFixed(2))
        assertEquals("0.07", 0.07.toFixed(2))
    }

    @Test
    fun zeroDecimals_dropsPoint() {
        assertEquals("3", 2.6.toFixed(0))
    }

    @Test
    fun negativeValues_keepSign() {
        assertEquals("-1.50", (-1.5).toFixed(2))
        assertEquals("0.00", (-0.001).toFixed(2))
    }
}
