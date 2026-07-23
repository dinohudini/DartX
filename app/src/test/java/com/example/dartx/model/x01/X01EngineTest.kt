package com.example.dartx.model.x01

import com.example.dartx.model.Dart
import com.example.dartx.model.InRule
import com.example.dartx.model.Multiplier
import com.example.dartx.model.OutRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class X01EngineTest {

    private fun d(value: Int, mult: Multiplier) = Dart(value, mult)

    @Test
    fun `straight in scores normally from the first dart`() {
        val start = TurnStartState(remaining = 501, isPlayerIn = false)
        val darts = listOf(d(20, Multiplier.TRIPLE), d(20, Multiplier.TRIPLE), d(20, Multiplier.TRIPLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.NORMAL, result.outcome)
        assertEquals(501 - 180, result.remainingAfter)
        assertTrue(result.isPlayerInAfter)
    }

    @Test
    fun `double in darts before the double do not count`() {
        val start = TurnStartState(remaining = 100, isPlayerIn = false)
        // Miss, miss, then double 10 (=20) to get in.
        val darts = listOf(d(5, Multiplier.SINGLE), d(19, Multiplier.SINGLE), d(10, Multiplier.DOUBLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.DOUBLE_IN)

        assertEquals(TurnResult.Outcome.NORMAL, result.outcome)
        assertEquals(100 - 20, result.remainingAfter)
        assertTrue(result.isPlayerInAfter)
    }

    @Test
    fun `double in with no double in the turn scores nothing and stays not in`() {
        val start = TurnStartState(remaining = 100, isPlayerIn = false)
        val darts = listOf(d(5, Multiplier.SINGLE), d(19, Multiplier.SINGLE), d(1, Multiplier.SINGLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.DOUBLE_IN)

        assertEquals(TurnResult.Outcome.NORMAL, result.outcome)
        assertEquals(100, result.remainingAfter)
        assertFalse(result.isPlayerInAfter)
    }

    @Test
    fun `getting in via double then busting later in the same turn stays in`() {
        val start = TurnStartState(remaining = 60, isPlayerIn = false)
        // D20 gets them in (remaining 20), then T20 overshoots -> bust.
        val darts = listOf(d(20, Multiplier.DOUBLE), d(20, Multiplier.TRIPLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.DOUBLE_IN)

        assertEquals(TurnResult.Outcome.BUST, result.outcome)
        assertEquals(60, result.remainingAfter)
        assertTrue(result.isPlayerInAfter)
    }

    @Test
    fun `double out exact zero on a double is a checkout`() {
        val start = TurnStartState(remaining = 40, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.DOUBLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.CHECKOUT, result.outcome)
        assertEquals(0, result.remainingAfter)
        assertEquals(1, result.dartsThrown)
    }

    @Test
    fun `double out exact zero on a single is a bust`() {
        val start = TurnStartState(remaining = 20, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.SINGLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.BUST, result.outcome)
        assertEquals(20, result.remainingAfter)
    }

    @Test
    fun `leaving exactly one is a bust under double out`() {
        val start = TurnStartState(remaining = 21, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.SINGLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.BUST, result.outcome)
        assertEquals(21, result.remainingAfter)
    }

    @Test
    fun `leaving exactly one is a bust under master out too`() {
        val start = TurnStartState(remaining = 21, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.SINGLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.MASTER_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.BUST, result.outcome)
    }

    @Test
    fun `leaving exactly one is fine under single out`() {
        val start = TurnStartState(remaining = 21, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.SINGLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.SINGLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.NORMAL, result.outcome)
        assertEquals(1, result.remainingAfter)
    }

    @Test
    fun `master out allows a triple to finish`() {
        val start = TurnStartState(remaining = 60, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.TRIPLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.MASTER_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.CHECKOUT, result.outcome)
    }

    @Test
    fun `master out rejects a single to finish`() {
        val start = TurnStartState(remaining = 20, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.SINGLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.MASTER_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.BUST, result.outcome)
    }

    @Test
    fun `single out allows any field to finish`() {
        val start = TurnStartState(remaining = 20, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.SINGLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.SINGLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.CHECKOUT, result.outcome)
    }

    @Test
    fun `overshooting the remaining score is a bust`() {
        val start = TurnStartState(remaining = 30, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.TRIPLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.BUST, result.outcome)
        assertEquals(30, result.remainingAfter)
    }

    @Test
    fun `checkout mid turn stops processing further darts`() {
        val start = TurnStartState(remaining = 40, isPlayerIn = true)
        val darts = listOf(d(20, Multiplier.DOUBLE), d(20, Multiplier.TRIPLE), d(20, Multiplier.TRIPLE))

        val result = X01Engine.applyTurn(start, darts, OutRule.DOUBLE_OUT, InRule.STRAIGHT_IN)

        assertEquals(TurnResult.Outcome.CHECKOUT, result.outcome)
        assertEquals(1, result.dartsThrown)
    }
}
