package com.dinohud.dartx.viewmodel

import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.data.local.Player
import com.dinohud.dartx.model.GameMode
import com.dinohud.dartx.model.InRule
import com.dinohud.dartx.model.OutRule
import com.dinohud.dartx.model.SetLegMode
import kotlin.test.Test
import kotlin.test.assertEquals

class PadModeTest {

    private fun match(inRule: InRule) = Match(
        id = 1,
        gameMode = GameMode.X01,
        startPoints = 501,
        outRule = OutRule.DOUBLE_OUT,
        inRule = inRule,
        setLegMode = SetLegMode.FIRST_TO,
        setsTarget = 1,
        legsTarget = 3
    )

    private fun board(id: Long, current: Boolean, isIn: Boolean) = PlayerBoard(
        player = Player(id = id, name = "P$id", avatarColor = "#F2795E"),
        remaining = 501,
        legWins = 0,
        totalLegWins = 0,
        setWins = 0,
        isCurrentPlayer = current,
        isIn = isIn,
        threeDartAverage = null
    )

    @Test
    fun `turn total switches to per dart while the current player is not in`() {
        val boards = listOf(board(1, current = true, isIn = false), board(2, current = false, isIn = true))

        assertEquals(ScoreInputMode.PER_DART, padModeFor(ScoreInputMode.TURN_TOTAL, match(InRule.DOUBLE_IN), boards))
    }

    @Test
    fun `turn total comes back once the current player is in`() {
        val boards = listOf(board(1, current = true, isIn = true), board(2, current = false, isIn = false))

        assertEquals(ScoreInputMode.TURN_TOTAL, padModeFor(ScoreInputMode.TURN_TOTAL, match(InRule.DOUBLE_IN), boards))
    }

    @Test
    fun `straight in never forces per dart`() {
        val boards = listOf(board(1, current = true, isIn = false), board(2, current = false, isIn = false))

        assertEquals(ScoreInputMode.TURN_TOTAL, padModeFor(ScoreInputMode.TURN_TOTAL, match(InRule.STRAIGHT_IN), boards))
    }

    @Test
    fun `per dart stays per dart`() {
        val boards = listOf(board(1, current = true, isIn = true), board(2, current = false, isIn = true))

        assertEquals(ScoreInputMode.PER_DART, padModeFor(ScoreInputMode.PER_DART, match(InRule.DOUBLE_IN), boards))
    }
}
