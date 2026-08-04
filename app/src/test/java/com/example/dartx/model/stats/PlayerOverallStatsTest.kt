package com.example.dartx.model.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayerOverallStatsTest {

    private fun matchStats(
        matchId: Long,
        isWinner: Boolean = false,
        legsPlayed: Int = 0,
        legsWon: Int = 0,
        totals: ScoringTotals = ScoringTotals.EMPTY
    ) = PlayerMatchStats(
        playerId = 1L,
        matchId = matchId,
        isWinner = isWinner,
        legsPlayed = legsPlayed,
        legsWon = legsWon,
        totals = totals
    )

    @Test
    fun `the overall average divides total points by total darts, not the per-match averages`() {
        val slow = matchStats(1L, totals = ScoringTotals(pointsScored = 600, dartsThrown = 30))
        val fast = matchStats(2L, totals = ScoringTotals(pointsScored = 1800, dartsThrown = 60))

        assertEquals(60.0, slow.totals.threeDartAverage!!, 0.0001)
        assertEquals(90.0, fast.totals.threeDartAverage!!, 0.0001)

        val overall = PlayerOverallStats.aggregate(1L, listOf(slow, fast))

        assertEquals(80.0, overall.totals.threeDartAverage!!, 0.0001)
    }

    @Test
    fun `extremes fold as a maximum and best leg darts as a minimum across matches`() {
        val first = matchStats(
            1L,
            totals = ScoringTotals(highestTurn = 140, highestCheckout = 96, bestLegDarts = 21, bestLegAverage = 71.5)
        )
        val second = matchStats(
            2L,
            totals = ScoringTotals(highestTurn = 180, highestCheckout = 64, bestLegDarts = 18, bestLegAverage = 83.4)
        )

        val overall = PlayerOverallStats.aggregate(1L, listOf(first, second)).totals

        assertEquals(180, overall.highestTurn)
        assertEquals(96, overall.highestCheckout)
        assertEquals(18, overall.bestLegDarts)
        assertEquals(83.4, overall.bestLegAverage!!, 0.0001)
    }

    @Test
    fun `a match with no won leg does not erase another match's best leg`() {
        val won = matchStats(1L, totals = ScoringTotals(bestLegDarts = 18, highestCheckout = 40))
        val lost = matchStats(2L, totals = ScoringTotals(bestLegDarts = null, highestCheckout = null))

        val overall = PlayerOverallStats.aggregate(1L, listOf(won, lost)).totals

        assertEquals(18, overall.bestLegDarts)
        assertEquals(40, overall.highestCheckout)
    }

    @Test
    fun `per-dart position totals add up across matches`() {
        val first = matchStats(
            1L,
            totals = ScoringTotals(dartsRecorded = 6, dartPoints = listOf(60, 40, 20), dartCounts = listOf(2, 2, 2))
        )
        val second = matchStats(
            2L,
            totals = ScoringTotals(dartsRecorded = 3, dartPoints = listOf(20, 0, 0), dartCounts = listOf(1, 1, 1))
        )

        val overall = PlayerOverallStats.aggregate(1L, listOf(first, second)).totals

        assertEquals(9, overall.dartsRecorded)
        assertEquals(listOf(80, 40, 20), overall.dartPoints)
        assertEquals(listOf(3, 3, 3), overall.dartCounts)
        assertEquals(80.0 / 3, overall.dartAverage(0)!!, 0.0001)
    }

    @Test
    fun `win rates come from the matches and legs that were counted`() {
        val overall = PlayerOverallStats.aggregate(
            1L,
            listOf(
                matchStats(1L, isWinner = true, legsPlayed = 3, legsWon = 2),
                matchStats(2L, isWinner = false, legsPlayed = 5, legsWon = 2)
            )
        )

        assertEquals(2, overall.matchesPlayed)
        assertEquals(1, overall.matchesWon)
        assertEquals(0.5, overall.winRate!!, 0.0001)
        assertEquals(8, overall.legsPlayed)
        assertEquals(4, overall.legsWon)
        assertEquals(0.5, overall.legWinRate!!, 0.0001)
    }

    @Test
    fun `a player with no counted matches divides nothing by zero`() {
        val overall = PlayerOverallStats.aggregate(1L, emptyList())

        assertEquals(0, overall.matchesPlayed)
        assertEquals(ScoringTotals.EMPTY, overall.totals)
        assertNull(overall.winRate)
        assertNull(overall.legWinRate)
        assertNull(overall.totals.threeDartAverage)
        assertNull(overall.totals.first9Average)
        assertNull(overall.totals.dartAverage(0))
    }
}
