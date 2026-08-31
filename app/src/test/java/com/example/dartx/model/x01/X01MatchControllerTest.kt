package com.example.dartx.model.x01

import com.example.dartx.model.Dart
import com.example.dartx.model.InRule
import com.example.dartx.model.Multiplier
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class X01MatchControllerTest {

    private fun d(value: Int, mult: Multiplier) = Dart(value, mult)

    @Test
    fun `winning a leg rotates who starts the next leg`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 1,
            legsTarget = 3
        )

        assertEquals(1L, controller.currentPlayerId)
        controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))

        assertEquals(2L, controller.currentPlayerId)
        assertEquals(1, controller.legWinsFor(1L))
        assertEquals(40, controller.remainingFor(1L))
    }

    @Test
    fun `first to 3 legs wins the set and match`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 1,
            legsTarget = 3
        )

        repeat(3) {
            while (controller.currentPlayerId != 1L) {
                controller.applyTurn(listOf(d(1, Multiplier.SINGLE)))
            }
            val outcome = controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))
            assertEquals(1L, outcome.legWonBy)
        }

        assertEquals(1L, controller.matchWinner)
    }

    @Test
    fun `total leg wins survive the reset that clinching a set performs`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 2,
            legsTarget = 2
        )

        repeat(4) {
            while (controller.currentPlayerId != 1L) {
                controller.applyTurn(listOf(d(1, Multiplier.SINGLE)))
            }
            controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))
        }

        assertEquals(1L, controller.matchWinner)
        assertEquals(2, controller.setWinsFor(1L))
        assertEquals(0, controller.legWinsFor(1L))
        assertEquals(4, controller.totalLegWinsFor(1L))
        assertEquals(0, controller.totalLegWinsFor(2L))
    }

    @Test
    fun `best of 5 legs is equivalent to first to 3`() {
        val bestOf = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.BEST_OF,
            setsTarget = 1,
            legsTarget = 5
        )

        repeat(2) {
            while (bestOf.currentPlayerId != 1L) {
                bestOf.applyTurn(listOf(d(1, Multiplier.SINGLE)))
            }
            bestOf.applyTurn(listOf(d(20, Multiplier.DOUBLE)))
        }
        assertNull(bestOf.matchWinner)

        while (bestOf.currentPlayerId != 1L) {
            bestOf.applyTurn(listOf(d(1, Multiplier.SINGLE)))
        }
        bestOf.applyTurn(listOf(d(20, Multiplier.DOUBLE)))

        assertEquals(1L, bestOf.matchWinner)
    }

    @Test
    fun `turn totals drive the match the same way individual darts do`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 100,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 1,
            legsTarget = 1
        )

        controller.applyTurnTotal(60)
        assertEquals(40, controller.remainingFor(1L))
        assertEquals(2L, controller.currentPlayerId)

        controller.applyTurnTotal(0)
        val outcome = controller.applyTurnTotal(40)

        assertEquals(1L, outcome.legWonBy)
        assertEquals(1L, controller.matchWinner)
    }

    @Test
    fun `the turn that wins a leg reports the leg it belonged to`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 1,
            legsTarget = 3
        )

        assertEquals(1, controller.applyTurn(listOf(d(1, Multiplier.SINGLE))).legNumber)

        val winning = controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))
        assertEquals(2L, winning.legWonBy)
        assertEquals(1, winning.legNumber)

        assertEquals(2, controller.applyTurn(listOf(d(1, Multiplier.SINGLE))).legNumber)
    }

    @Test
    fun `leg numbering does not restart when a set is clinched`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 2,
            legsTarget = 2
        )

        val legNumbers = mutableListOf<Int>()
        repeat(4) {
            while (controller.currentPlayerId != 1L) {
                controller.applyTurn(listOf(d(1, Multiplier.SINGLE)))
            }
            legNumbers += controller.applyTurn(listOf(d(20, Multiplier.DOUBLE))).legNumber
        }

        assertEquals(listOf(1, 2, 3, 4), legNumbers)
    }

    @Test
    fun `bust does not end the leg and passes the turn on`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L, 3L),
            startPoints = 20,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 1,
            legsTarget = 1
        )

        val outcome = controller.applyTurn(listOf(d(20, Multiplier.SINGLE)))

        assertEquals(TurnResult.Outcome.BUST, outcome.turnResult.outcome)
        assertNull(outcome.legWonBy)
        assertEquals(20, controller.remainingFor(1L))
        assertEquals(2L, controller.currentPlayerId)
    }

    @Test
    fun `restoring a snapshot takes back an ordinary turn`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L, 3L),
            startPoints = 501,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.DOUBLE_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 1,
            legsTarget = 3
        )

        controller.applyTurn(listOf(d(20, Multiplier.DOUBLE), d(20, Multiplier.TRIPLE)))
        val before = controller.snapshot()

        assertEquals(2L, controller.currentPlayerId)
        controller.applyTurn(listOf(d(19, Multiplier.DOUBLE), d(19, Multiplier.TRIPLE)))
        controller.restore(before)

        assertEquals(2L, controller.currentPlayerId)
        assertEquals(501, controller.remainingFor(2L))
        assertEquals(false, controller.isInFor(2L))
        assertEquals(401, controller.remainingFor(1L))
        assertEquals(true, controller.isInFor(1L))
    }

    @Test
    fun `restoring a snapshot takes back the turn that won the match`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 2,
            legsTarget = 1
        )

        controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))
        val before = controller.snapshot()

        while (controller.currentPlayerId != 1L) {
            controller.applyTurn(listOf(d(1, Multiplier.SINGLE)))
        }
        controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))
        assertEquals(1L, controller.matchWinner)

        controller.restore(before)

        assertNull(controller.matchWinner)
        assertEquals(1, controller.setWinsFor(1L))
        assertEquals(1, controller.totalLegWinsFor(1L))
        assertEquals(0, controller.legWinsFor(1L))
        assertEquals(2L, controller.currentPlayerId)
        assertEquals(40, controller.remainingFor(1L))
        assertEquals(2L, controller.applyTurn(listOf(d(1, Multiplier.SINGLE))).playerId)
    }

    @Test
    fun `a leg replayed after a rollback lands in the same leg number`() {
        val controller = X01MatchController(
            playerIds = listOf(1L, 2L),
            startPoints = 40,
            outRule = OutRule.DOUBLE_OUT,
            inRule = InRule.STRAIGHT_IN,
            setLegMode = SetLegMode.FIRST_TO,
            setsTarget = 1,
            legsTarget = 3
        )

        val before = controller.snapshot()
        val first = controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))
        controller.restore(before)
        val replayed = controller.applyTurn(listOf(d(20, Multiplier.DOUBLE)))

        assertEquals(first.legNumber, replayed.legNumber)
        assertEquals(1, controller.legWinsFor(1L))
        assertEquals(1, controller.totalLegWinsFor(1L))
    }
}
