package com.example.dartx.model.stats

import com.example.dartx.data.local.Match
import com.example.dartx.data.local.Throw
import com.example.dartx.model.Dart
import com.example.dartx.model.GameMode
import com.example.dartx.model.InRule
import com.example.dartx.model.Multiplier
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode
import com.example.dartx.model.ThrowStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val MATCH_ID = 7L
private const val PLAYER = 1L
private const val OPPONENT = 2L

private fun match(
    startPoints: Int,
    legWins: List<Int> = listOf(1, 0),
    winnerPlayerId: Long? = PLAYER,
    gameMode: GameMode = GameMode.X01
) = Match(
    id = MATCH_ID,
    gameMode = gameMode,
    startPoints = startPoints,
    outRule = OutRule.DOUBLE_OUT,
    inRule = InRule.STRAIGHT_IN,
    setLegMode = SetLegMode.FIRST_TO,
    setsTarget = 1,
    legsTarget = legWins.max(),
    participantIds = listOf(PLAYER, OPPONENT),
    setWins = listOf(1, 0),
    legWins = legWins,
    winnerPlayerId = winnerPlayerId
)

private fun d(value: Int, mult: Multiplier = Multiplier.SINGLE) = Dart(value, mult)

private class ThrowLog(private val playerId: Long = PLAYER) {
    val rows = mutableListOf<Throw>()
    private var turnNumber = 0

    fun turn(leg: Int, vararg darts: Dart, status: ThrowStatus = ThrowStatus.COUNTED): ThrowLog {
        turnNumber += 1
        darts.forEach { dart -> rows += row(leg, dart.fieldValue, dart.multiplier, dart.score, status) }
        return this
    }

    fun mixedTurn(leg: Int, vararg darts: Pair<Dart, ThrowStatus>): ThrowLog {
        turnNumber += 1
        darts.forEach { (dart, status) ->
            rows += row(leg, dart.fieldValue, dart.multiplier, dart.score, status)
        }
        return this
    }

    fun total(leg: Int, score: Int, status: ThrowStatus = ThrowStatus.COUNTED): ThrowLog {
        turnNumber += 1
        rows += row(leg, null, null, score, status)
        return this
    }

    private fun row(leg: Int, field: Int?, mult: Multiplier?, score: Int, status: ThrowStatus) = Throw(
        matchId = MATCH_ID,
        playerId = playerId,
        turnNumber = turnNumber,
        fieldValue = field,
        multiplier = mult,
        score = score,
        legNumber = leg,
        status = status
    )
}

class X01MatchStatsTest {

    private fun totalsOf(match: Match, log: ThrowLog): ScoringTotals =
        X01MatchStats.compute(match, log.rows).getValue(PLAYER).totals

    @Test
    fun `a clean leg of three turns totals its points darts and extremes`() {
        val match = match(startPoints = 120)
        val log = ThrowLog()
            .turn(1, d(20, Multiplier.TRIPLE), Dart.MISS, Dart.MISS)
            .turn(1, d(20), Dart.MISS, Dart.MISS)
            .turn(1, d(20, Multiplier.DOUBLE))

        val totals = totalsOf(match, log)

        assertEquals(120, totals.pointsScored)
        assertEquals(7, totals.dartsThrown)
        assertEquals(120 * 3.0 / 7, totals.threeDartAverage!!, 0.0001)
        assertEquals(60, totals.highestTurn)
        assertEquals(40, totals.highestCheckout)
        assertEquals(7, totals.bestLegDarts)
        assertEquals(120 * 3.0 / 7, totals.bestLegAverage!!, 0.0001)
    }

    @Test
    fun `a won leg is recognised from the counted points alone`() {
        val match = match(startPoints = 120)
        val log = ThrowLog()
            .turn(1, d(20, Multiplier.TRIPLE), Dart.MISS, Dart.MISS)
            .turn(1, d(20), Dart.MISS, Dart.MISS)
            .turn(1, d(20, Multiplier.DOUBLE))

        val totals = totalsOf(match, log)

        assertEquals(match.startPoints, totals.pointsScored)
        assertEquals(7, totals.bestLegDarts)
    }

    @Test
    fun `a leg that was not closed leaves the won-leg extremes unset`() {
        val match = match(startPoints = 501, legWins = listOf(0, 1), winnerPlayerId = OPPONENT)
        val log = ThrowLog().turn(1, d(20, Multiplier.TRIPLE), d(20, Multiplier.TRIPLE), d(20, Multiplier.TRIPLE))

        val totals = totalsOf(match, log)

        assertNull(totals.bestLegDarts)
        assertNull(totals.bestLegAverage)
        assertNull(totals.highestCheckout)
    }

    @Test
    fun `a busted turn scores nothing but its darts still count`() {
        val match = match(startPoints = 120)
        val log = ThrowLog()
            .turn(1, d(20, Multiplier.TRIPLE), Dart.MISS, Dart.MISS)
            .turn(1, d(20, Multiplier.TRIPLE), status = ThrowStatus.BUST)
            .turn(1, d(20), Dart.MISS, Dart.MISS)
            .turn(1, d(20, Multiplier.DOUBLE))

        val totals = totalsOf(match, log)

        assertEquals(120, totals.pointsScored)
        assertEquals(8, totals.dartsThrown)
        assertEquals(60, totals.highestTurn)
        assertEquals(1, totals.count60Plus)
        assertEquals(4, totals.hits20or19)
        assertEquals(8, totals.dartsRecorded)
    }

    @Test
    fun `not-in darts score nothing but still land on the board`() {
        val match = match(startPoints = 40)
        val log = ThrowLog()
            .turn(1, d(5), d(19), d(1), status = ThrowStatus.NOT_IN)
            .turn(1, d(20, Multiplier.DOUBLE))

        val totals = totalsOf(match, log)

        assertEquals(40, totals.pointsScored)
        assertEquals(4, totals.dartsThrown)
        assertEquals(2, totals.hits20or19)
        assertEquals(0.5, totals.hitRate20or19!!, 0.0001)
        assertEquals(listOf(2, 1, 1), totals.dartCounts)
        assertEquals(listOf(40, 0, 0), totals.dartPoints)
        assertEquals(20.0, totals.dartAverage(0)!!, 0.0001)
    }

    @Test
    fun `a bust wins over not-in for every dart of the turn`() {
        val match = match(startPoints = 60, legWins = listOf(0, 1), winnerPlayerId = OPPONENT)
        val log = ThrowLog().mixedTurn(
            1,
            d(5) to ThrowStatus.BUST,
            d(20, Multiplier.DOUBLE) to ThrowStatus.BUST,
            d(20, Multiplier.TRIPLE) to ThrowStatus.BUST
        )

        val totals = totalsOf(match, log)

        assertEquals(0, totals.pointsScored)
        assertEquals(3, totals.dartsThrown)
        assertEquals(3, totals.dartsRecorded)
    }

    @Test
    fun `a turn-total match reports exact points and checkout but no per-dart data`() {
        val match = match(startPoints = 301)
        val log = ThrowLog()
            .total(1, 100)
            .total(1, 100)
            .total(1, 101)

        val totals = totalsOf(match, log)

        assertEquals(301, totals.pointsScored)
        assertEquals(9, totals.dartsThrown)
        assertEquals(101, totals.highestTurn)
        assertEquals(101, totals.highestCheckout)
        assertEquals(9, totals.bestLegDarts)
        assertEquals(0, totals.dartsRecorded)
        assertNull(totals.hitRate20or19)
        assertNull(totals.tripleRate)
        assertNull(totals.dartAverage(0))
    }

    @Test
    fun `one per-dart turn is enough to produce per-dart data in a mixed leg`() {
        val match = match(startPoints = 100)
        val log = ThrowLog()
            .total(1, 60)
            .turn(1, d(20, Multiplier.DOUBLE))

        val totals = totalsOf(match, log)

        assertEquals(100, totals.pointsScored)
        assertEquals(4, totals.dartsThrown)
        assertEquals(1, totals.dartsRecorded)
        assertEquals(40.0, totals.dartAverage(0)!!, 0.0001)
        assertNull(totals.dartAverage(1))
    }

    @Test
    fun `scoring baskets are cumulative and a busted 180 counts for none of them`() {
        val match = match(startPoints = 501, legWins = listOf(0, 1), winnerPlayerId = OPPONENT)
        val triple20 = arrayOf(d(20, Multiplier.TRIPLE), d(20, Multiplier.TRIPLE), d(20, Multiplier.TRIPLE))
        val log = ThrowLog()
            .turn(1, *triple20)
            .turn(1, *triple20, status = ThrowStatus.BUST)
            .turn(1, d(20, Multiplier.TRIPLE), d(20), d(20))

        val totals = totalsOf(match, log)

        assertEquals(1, totals.count180)
        assertEquals(1, totals.count140Plus)
        assertEquals(2, totals.count100Plus)
        assertEquals(2, totals.count60Plus)
        assertEquals(180, totals.highestTurn)
    }

    @Test
    fun `first nine covers the opening three turns of every leg`() {
        val match = match(startPoints = 100, legWins = listOf(1, 1))
        val log = ThrowLog()
            .total(1, 60)
            .total(2, 100)

        val totals = totalsOf(match, log)

        assertEquals(160, totals.first9Points)
        assertEquals(6, totals.first9Darts)
        assertEquals(80.0, totals.first9Average!!, 0.0001)
        assertEquals(3, totals.bestLegDarts)
        assertEquals(100.0, totals.bestLegAverage!!, 0.0001)
        assertEquals(100, totals.highestCheckout)
    }

    @Test
    fun `dart positions are counted separately when a turn ends on one dart`() {
        val match = match(startPoints = 120)
        val log = ThrowLog()
            .turn(1, d(20, Multiplier.TRIPLE), Dart.MISS, Dart.MISS)
            .turn(1, d(20), Dart.MISS, Dart.MISS)
            .turn(1, d(20, Multiplier.DOUBLE))

        val totals = totalsOf(match, log)

        assertEquals(listOf(3, 2, 2), totals.dartCounts)
        assertEquals(listOf(120, 0, 0), totals.dartPoints)
        assertEquals(40.0, totals.dartAverage(0)!!, 0.0001)
        assertEquals(0.0, totals.dartAverage(1)!!, 0.0001)
    }

    @Test
    fun `legs recorded on the throws agree with the snapshot on the match`() {
        val match = match(startPoints = 100, legWins = listOf(1, 1))
        val log = ThrowLog()
            .total(1, 100)
            .total(2, 40)

        val stats = X01MatchStats.compute(match, log.rows).getValue(PLAYER)

        assertEquals(log.rows.map { it.legNumber }.distinct().size, stats.legsPlayed)
        assertEquals(2, stats.legsPlayed)
        assertEquals(1, stats.legsWon)
    }

    @Test
    fun `a player who threw nothing still gets a row of zeroes`() {
        val match = match(startPoints = 100)
        val log = ThrowLog().total(1, 100)

        val opponent = X01MatchStats.compute(match, log.rows).getValue(OPPONENT)

        assertEquals(ScoringTotals.EMPTY, opponent.totals)
        assertEquals(0, opponent.legsWon)
        assertFalse(opponent.isWinner)
    }

    @Test
    fun `only finished X01 matches count towards statistics`() {
        assertTrue(X01MatchStats.countsForStats(match(startPoints = 501)))
        assertFalse(X01MatchStats.countsForStats(match(startPoints = 501, winnerPlayerId = null)))
        assertFalse(
            X01MatchStats.countsForStats(match(startPoints = 501, gameMode = GameMode.CRICKET))
        )
    }
}
