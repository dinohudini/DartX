package com.dinohud.dartx.ui.screens

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScoreboardZoneTest {

    private val compactPhone = 662.dp
    private val tallPhone = 900.dp

    @Test
    fun `a compact phone shows two cards and scrolls the rest`() {
        val metrics = metricsFor(compactPhone, boardCount = 4, perDart = true)

        assertEquals(cardSpan(2, metrics.cardHeight), metrics.zoneHeight)
    }

    @Test
    fun `a tall phone shows four players without scrolling`() {
        val metrics = metricsFor(tallPhone, boardCount = 4, perDart = true)

        assertEquals(cardSpan(4, metrics.cardHeight), metrics.zoneHeight)
        assertEquals(100.dp, metrics.cardHeight)
    }

    @Test
    fun `two players keep full size cards on a tall phone`() {
        val metrics = metricsFor(tallPhone, boardCount = 2, perDart = false)

        assertEquals(100.dp, metrics.cardHeight)
        assertEquals(cardSpan(2, 100.dp), metrics.zoneHeight)
    }

    @Test
    fun `a crowded match never pushes the zone into the pad`() {
        listOf(compactPhone, tallPhone).forEach { available ->
            listOf(true, false).forEach { perDart ->
                val metrics = metricsFor(available, boardCount = 8, perDart = perDart)
                val budget = available - metrics.pad.height - metrics.pad.stripHeight(perDart)

                assertTrue(metrics.zoneHeight <= budget)
                assertTrue(metrics.cardHeight >= 80.dp)
            }
        }
    }
}
