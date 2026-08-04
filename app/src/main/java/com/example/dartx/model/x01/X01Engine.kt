package com.example.dartx.model.x01

import com.example.dartx.model.Dart
import com.example.dartx.model.InRule
import com.example.dartx.model.OutRule
import com.example.dartx.model.ThrowStatus

data class TurnStartState(val remaining: Int, val isPlayerIn: Boolean)

data class TurnResult(
    val remainingAfter: Int,
    val isPlayerInAfter: Boolean,
    val dartsThrown: Int,
    val outcome: Outcome,
    val throwStatuses: List<ThrowStatus> = emptyList()
) {
    enum class Outcome { NORMAL, BUST, CHECKOUT }
}

/**
 * Pure turn-scoring logic for 501/301 (and custom start points).
 * Stateless: takes the state at the start of a turn and the darts thrown, returns the result.
 */
object X01Engine {

    /**
     * Darts attributed to a turn entered as a bare total. The real count is unknown with fast
     * entry, and 3 is both the common case and the divisor a 3-dart average expects.
     */
    const val NOMINAL_DARTS_PER_TURN = 3

    fun applyTurn(
        start: TurnStartState,
        darts: List<Dart>,
        outRule: OutRule,
        inRule: InRule
    ): TurnResult {
        require(darts.isNotEmpty() && darts.size <= 3) { "A turn is 1-3 darts" }

        var remaining = start.remaining
        var isIn = start.isPlayerIn
        var becameInThisTurn = false
        val statuses = mutableListOf<ThrowStatus>()

        for ((index, dart) in darts.withIndex()) {
            if (!isIn) {
                if (inRule == InRule.STRAIGHT_IN) {
                    isIn = true
                } else if (dart.isDouble) {
                    isIn = true
                    becameInThisTurn = true
                } else {
                    // Double-in not yet achieved: dart is thrown but doesn't count.
                    statuses += ThrowStatus.NOT_IN
                    continue
                }
            }

            statuses += ThrowStatus.COUNTED

            val newRemaining = remaining - dart.score
            val leavesUnfinishableOne = newRemaining == 1 && outRule != OutRule.SINGLE_OUT

            if (newRemaining < 0 || leavesUnfinishableOne) {
                // Bust: whole turn's score is voided, but a double hit earlier in
                // this same turn still legitimately got the player "in".
                return TurnResult(
                    remainingAfter = start.remaining,
                    isPlayerInAfter = start.isPlayerIn || becameInThisTurn,
                    dartsThrown = index + 1,
                    outcome = TurnResult.Outcome.BUST,
                    throwStatuses = List(index + 1) { ThrowStatus.BUST }
                )
            }

            if (newRemaining == 0) {
                val validFinish = when (outRule) {
                    OutRule.DOUBLE_OUT -> dart.isDouble
                    OutRule.MASTER_OUT -> dart.isDouble || dart.isTriple
                    OutRule.SINGLE_OUT -> true
                }
                return if (validFinish) {
                    TurnResult(
                        remainingAfter = 0,
                        isPlayerInAfter = true,
                        dartsThrown = index + 1,
                        outcome = TurnResult.Outcome.CHECKOUT,
                        throwStatuses = statuses.toList()
                    )
                } else {
                    TurnResult(
                        remainingAfter = start.remaining,
                        isPlayerInAfter = start.isPlayerIn || becameInThisTurn,
                        dartsThrown = index + 1,
                        outcome = TurnResult.Outcome.BUST,
                        throwStatuses = List(index + 1) { ThrowStatus.BUST }
                    )
                }
            }

            remaining = newRemaining
        }

        return TurnResult(
            remainingAfter = remaining,
            isPlayerInAfter = isIn,
            dartsThrown = darts.size,
            outcome = TurnResult.Outcome.NORMAL,
            throwStatuses = statuses.toList()
        )
    }

    /**
     * Fast entry: a whole turn given as a single total, with the individual fields unknown.
     *
     * Without the fields the engine cannot check the in/out rules itself, so the player asserts
     * them at the board: under double-in any non-zero total is taken as proof they got in, and
     * landing on exactly zero is taken as a legal finish. The bust rules that depend only on the
     * total — dropping below zero, or leaving exactly 1 — are still enforced here.
     */
    fun applyTurnTotal(
        start: TurnStartState,
        total: Int,
        outRule: OutRule,
        inRule: InRule
    ): TurnResult {
        require(total in 0..180) { "A turn total is 0-180" }

        val isInAfter = start.isPlayerIn || inRule == InRule.STRAIGHT_IN || total > 0
        if (!isInAfter) {
            return TurnResult(
                start.remaining, false, NOMINAL_DARTS_PER_TURN, TurnResult.Outcome.NORMAL,
                listOf(ThrowStatus.NOT_IN)
            )
        }

        val newRemaining = start.remaining - total
        val leavesUnfinishableOne = newRemaining == 1 && outRule != OutRule.SINGLE_OUT

        return when {
            newRemaining < 0 || leavesUnfinishableOne ->
                TurnResult(
                    start.remaining, isInAfter, NOMINAL_DARTS_PER_TURN, TurnResult.Outcome.BUST,
                    listOf(ThrowStatus.BUST)
                )

            newRemaining == 0 ->
                TurnResult(
                    0, true, NOMINAL_DARTS_PER_TURN, TurnResult.Outcome.CHECKOUT,
                    listOf(ThrowStatus.COUNTED)
                )

            else ->
                TurnResult(
                    newRemaining, isInAfter, NOMINAL_DARTS_PER_TURN, TurnResult.Outcome.NORMAL,
                    listOf(ThrowStatus.COUNTED)
                )
        }
    }
}
