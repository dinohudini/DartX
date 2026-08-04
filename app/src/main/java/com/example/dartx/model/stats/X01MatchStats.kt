package com.example.dartx.model.stats

import com.example.dartx.data.local.Match
import com.example.dartx.data.local.Throw
import com.example.dartx.model.GameMode
import com.example.dartx.model.Multiplier
import com.example.dartx.model.ThrowStatus
import com.example.dartx.model.x01.X01Engine

object X01MatchStats {

    fun countsForStats(match: Match): Boolean =
        match.winnerPlayerId != null && match.gameMode == GameMode.X01

    fun compute(match: Match, throws: List<Throw>): Map<Long, PlayerMatchStats> {
        val legsPlayed = match.legWins.sum()
        val byPlayer = throws.groupBy { it.playerId }

        return match.participantIds.withIndex().associate { (index, playerId) ->
            playerId to PlayerMatchStats(
                playerId = playerId,
                matchId = match.id,
                isWinner = playerId == match.winnerPlayerId,
                legsPlayed = legsPlayed,
                legsWon = match.legWins.getOrElse(index) { 0 },
                totals = totalsFor(match, byPlayer[playerId].orEmpty())
            )
        }
    }
}

private class Turn(val rows: List<Throw>) {
    val legNumber: Int = rows.first().legNumber
    val isTurnTotal: Boolean = rows.first().fieldValue == null
    val darts: Int = if (isTurnTotal) X01Engine.NOMINAL_DARTS_PER_TURN else rows.size
    val points: Int = rows.filter { it.status == ThrowStatus.COUNTED }.sumOf { it.score }
}

private fun totalsFor(match: Match, playerThrows: List<Throw>): ScoringTotals {
    if (playerThrows.isEmpty()) return ScoringTotals.EMPTY

    val turns = playerThrows.groupBy { it.turnNumber }.values.map(::Turn)

    var first9Points = 0
    var first9Darts = 0
    var highestCheckout: Int? = null
    var bestLegAverage: Double? = null
    var bestLegDarts: Int? = null

    turns.groupBy { it.legNumber }.values.forEach { legTurns ->
        val opening = legTurns.take(3)
        first9Points += opening.sumOf { it.points }
        first9Darts += opening.sumOf { it.darts }

        val legPoints = legTurns.sumOf { it.points }
        if (legPoints == match.startPoints) {
            val legDarts = legTurns.sumOf { it.darts }
            highestCheckout = maxOf(highestCheckout ?: 0, legTurns.last().points)
            bestLegAverage = maxOf(bestLegAverage ?: 0.0, legPoints * 3.0 / legDarts)
            bestLegDarts = minOf(bestLegDarts ?: legDarts, legDarts)
        }
    }

    val dartRows = playerThrows.filter { it.fieldValue != null }
    val dartPoints = MutableList(DART_POSITIONS) { 0 }
    val dartCounts = MutableList(DART_POSITIONS) { 0 }

    turns.filterNot { it.isTurnTotal }.forEach { turn ->
        turn.rows.take(DART_POSITIONS).forEachIndexed { position, row ->
            dartCounts[position] += 1
            if (row.status == ThrowStatus.COUNTED) dartPoints[position] += row.score
        }
    }

    return ScoringTotals(
        pointsScored = turns.sumOf { it.points },
        dartsThrown = turns.sumOf { it.darts },
        first9Points = first9Points,
        first9Darts = first9Darts,
        highestTurn = turns.maxOf { it.points },
        highestCheckout = highestCheckout,
        bestLegAverage = bestLegAverage,
        bestLegDarts = bestLegDarts,
        count60Plus = turns.count { it.points >= 60 },
        count100Plus = turns.count { it.points >= 100 },
        count140Plus = turns.count { it.points >= 140 },
        count180 = turns.count { it.points >= 180 },
        dartsRecorded = dartRows.size,
        dartPoints = dartPoints,
        dartCounts = dartCounts,
        hits20or19 = dartRows.count { it.fieldValue == 20 || it.fieldValue == 19 },
        hitsTriple20or19 = dartRows.count {
            it.multiplier == Multiplier.TRIPLE && (it.fieldValue == 20 || it.fieldValue == 19)
        },
        hitsTriple = dartRows.count { it.multiplier == Multiplier.TRIPLE }
    )
}
