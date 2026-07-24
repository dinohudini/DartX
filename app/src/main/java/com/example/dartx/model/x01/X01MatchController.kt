package com.example.dartx.model.x01

import com.example.dartx.model.Dart
import com.example.dartx.model.InRule
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode

data class X01TurnOutcome(
    val playerId: Long,
    val turnResult: TurnResult,
    val legWonBy: Long? = null,
    val setWonBy: Long? = null,
    val matchWonBy: Long? = null
)

/**
 * Drives a full 501/301 match (flexible player count) turn by turn on top of [X01Engine]:
 * turn rotation, leg/set win counting, and who starts the next leg.
 *
 * Who starts the first leg of each set alternates strictly by rotation, independent of
 * who won the previous leg (standard darts convention).
 */
class X01MatchController(
    playerIds: List<Long>,
    private val startPoints: Int,
    private val outRule: OutRule,
    private val inRule: InRule,
    setLegMode: SetLegMode,
    setsTarget: Int,
    legsTarget: Int
) {
    private val order = playerIds.toList()

    init {
        require(order.size >= 2) { "A match needs at least 2 players" }
    }

    private val legWinsNeeded = setLegMode.winsNeeded(legsTarget)
    private val setWinsNeeded = setLegMode.winsNeeded(setsTarget)

    private val setWins = order.associateWith { 0 }.toMutableMap()
    private val legWins = order.associateWith { 0 }.toMutableMap()
    private val remaining = order.associateWith { startPoints }.toMutableMap()
    private val isIn = order.associateWith { false }.toMutableMap()

    private var legStartIndex = 0
    private var turnIndex = 0

    var matchWinner: Long? = null
        private set

    val currentPlayerId: Long get() = order[turnIndex]

    /** State the current player's turn starts from — lets the UI preview a turn dart by dart. */
    fun currentTurnStart(): TurnStartState =
        TurnStartState(remaining.getValue(currentPlayerId), isIn.getValue(currentPlayerId))

    fun applyTurn(darts: List<Dart>): X01TurnOutcome {
        check(matchWinner == null) { "Match already finished" }
        return commit(X01Engine.applyTurn(currentTurnStart(), darts, outRule, inRule))
    }

    /** Fast entry variant — see [X01Engine.applyTurnTotal] for what the total can and cannot prove. */
    fun applyTurnTotal(total: Int): X01TurnOutcome {
        check(matchWinner == null) { "Match already finished" }
        return commit(X01Engine.applyTurnTotal(currentTurnStart(), total, outRule, inRule))
    }

    private fun commit(result: TurnResult): X01TurnOutcome {
        val playerId = currentPlayerId

        remaining[playerId] = result.remainingAfter
        isIn[playerId] = result.isPlayerInAfter

        var legWonBy: Long? = null
        var setWonBy: Long? = null

        if (result.outcome == TurnResult.Outcome.CHECKOUT) {
            legWonBy = playerId
            legWins[playerId] = legWins.getValue(playerId) + 1

            if (legWins.getValue(playerId) >= legWinsNeeded) {
                setWonBy = playerId
                setWins[playerId] = setWins.getValue(playerId) + 1
                legWins.keys.forEach { legWins[it] = 0 }

                if (setWins.getValue(playerId) >= setWinsNeeded) {
                    matchWinner = playerId
                }
            }

            order.forEach { remaining[it] = startPoints; isIn[it] = false }
            legStartIndex = (legStartIndex + 1) % order.size
            turnIndex = legStartIndex
        } else {
            turnIndex = (turnIndex + 1) % order.size
        }

        return X01TurnOutcome(
            playerId = playerId,
            turnResult = result,
            legWonBy = legWonBy,
            setWonBy = setWonBy,
            matchWonBy = matchWinner
        )
    }

    fun remainingFor(playerId: Long): Int = remaining.getValue(playerId)
    fun legWinsFor(playerId: Long): Int = legWins.getValue(playerId)
    fun setWinsFor(playerId: Long): Int = setWins.getValue(playerId)

    /** Whether [playerId] has satisfied the in rule (always true under straight in). */
    fun isInFor(playerId: Long): Boolean = isIn.getValue(playerId)
}
