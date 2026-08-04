package com.example.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dartx.data.local.Match
import com.example.dartx.data.local.Player
import com.example.dartx.data.local.Throw
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository
import com.example.dartx.model.Dart
import com.example.dartx.model.x01.TurnResult
import com.example.dartx.model.x01.X01Engine
import com.example.dartx.model.x01.X01MatchController
import com.example.dartx.model.x01.X01TurnOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The two entry modes from the spec: a bare turn total, or every dart individually. */
enum class ScoreInputMode { TURN_TOTAL, PER_DART }

data class PlayerBoard(
    val player: Player,
    val remaining: Int,
    val legWins: Int,
    val setWins: Int,
    val isCurrentPlayer: Boolean,
    val isIn: Boolean,
    val threeDartAverage: Double?
)

data class LiveMatchUiState(
    val isLoading: Boolean = true,
    val match: Match? = null,
    val boards: List<PlayerBoard> = emptyList(),
    val inputMode: ScoreInputMode = ScoreInputMode.TURN_TOTAL,
    val pendingDarts: List<Dart> = emptyList(),
    /** Remaining for the current player part-way through a per-dart turn, before it is committed. */
    val previewRemaining: Int? = null,
    val message: String? = null,
    val winner: Player? = null,
    val error: String? = null
)

/**
 * Drives one live X01 match: wraps [X01MatchController] for the rules and writes every dart
 * to the repository as it is entered.
 */
class LiveMatchViewModel(
    private val matchId: Long,
    private val matchRepository: MatchRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveMatchUiState())
    val uiState: StateFlow<LiveMatchUiState> = _uiState.asStateFlow()

    private var controller: X01MatchController? = null
    private var match: Match? = null
    private var players: List<Player> = emptyList()

    /** Turn counter per player, so [Throw.turnNumber] groups the darts of one turn together. */
    private val turnNumbers = mutableMapOf<Long, Int>()

    private val pointsScored = mutableMapOf<Long, Int>()
    private val dartsThrown = mutableMapOf<Long, Int>()

    /** Keeps concurrent turns from interleaving their inserts, so throw ids stay in throw order. */
    private val writeLock = Mutex()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val loaded = matchRepository.getMatchById(matchId)
        if (loaded == null) {
            _uiState.update { it.copy(isLoading = false, error = "Match not found") }
            return
        }
        match = loaded
        players = loaded.participantIds.mapNotNull { playerRepository.getPlayerById(it) }

        if (players.size < 2) {
            _uiState.update {
                it.copy(isLoading = false, match = loaded, error = "This match's players no longer exist")
            }
            return
        }

        controller = X01MatchController(
            playerIds = players.map { it.id },
            startPoints = loaded.startPoints,
            outRule = loaded.outRule,
            inRule = loaded.inRule,
            setLegMode = loaded.setLegMode,
            setsTarget = loaded.setsTarget,
            legsTarget = loaded.legsTarget
        )
        _uiState.update { it.copy(isLoading = false, match = loaded, boards = boards()) }
    }

    fun setInputMode(mode: ScoreInputMode) {
        // Darts staged in the other mode would be lost silently otherwise.
        _uiState.update { it.copy(inputMode = mode, pendingDarts = emptyList(), previewRemaining = null) }
    }

    /**
     * Stages one dart. The turn is committed as soon as it can no longer change — three darts
     * thrown, or a bust/checkout — so the player never has to confirm an already-decided turn.
     */
    fun addDart(dart: Dart) {
        val controller = controller ?: return
        val match = match ?: return
        if (_uiState.value.winner != null) return

        val darts = _uiState.value.pendingDarts + dart
        val preview = X01Engine.applyTurn(controller.currentTurnStart(), darts, match.outRule, match.inRule)

        if (preview.outcome != TurnResult.Outcome.NORMAL || darts.size == 3) {
            commitDarts(darts)
        } else {
            _uiState.update {
                it.copy(pendingDarts = darts, previewRemaining = preview.remainingAfter, message = null)
            }
        }
    }

    fun undoPendingDart() {
        val controller = controller ?: return
        val match = match ?: return

        val darts = _uiState.value.pendingDarts.dropLast(1)
        val remaining = if (darts.isEmpty()) {
            null
        } else {
            X01Engine.applyTurn(controller.currentTurnStart(), darts, match.outRule, match.inRule).remainingAfter
        }
        _uiState.update { it.copy(pendingDarts = darts, previewRemaining = remaining) }
    }

    /** Ends a turn on fewer than three darts (a player who stops early, e.g. after a checkout attempt). */
    fun endTurnEarly() {
        val darts = _uiState.value.pendingDarts
        if (darts.isNotEmpty()) commitDarts(darts)
    }

    private fun commitDarts(darts: List<Dart>) {
        val controller = controller ?: return
        val scoreBefore = controller.currentTurnStart().remaining
        val outcome = controller.applyTurn(darts)
        val result = outcome.turnResult
        val turnNumber = nextTurnNumber(outcome.playerId)

        val rows = darts.take(result.dartsThrown).mapIndexed { index, dart ->
            Throw(
                matchId = matchId,
                playerId = outcome.playerId,
                turnNumber = turnNumber,
                fieldValue = dart.fieldValue,
                multiplier = dart.multiplier,
                score = dart.score,
                legNumber = outcome.legNumber,
                status = result.throwStatuses[index]
            )
        }
        publish(outcome, scored = scoreBefore - result.remainingAfter, rows = rows)
    }

    fun submitTurnTotal(total: Int) {
        val controller = controller ?: return
        if (_uiState.value.winner != null) return
        if (total !in 0..180) {
            _uiState.update { it.copy(message = "A turn total must be between 0 and 180") }
            return
        }

        val scoreBefore = controller.currentTurnStart().remaining
        val outcome = controller.applyTurnTotal(total)
        val result = outcome.turnResult
        val row = Throw(
            matchId = matchId,
            playerId = outcome.playerId,
            turnNumber = nextTurnNumber(outcome.playerId),
            // Fast entry knows the total but not the fields, which is exactly the generic row.
            fieldValue = null,
            multiplier = null,
            score = total,
            legNumber = outcome.legNumber,
            status = result.throwStatuses.first()
        )
        publish(outcome, scored = scoreBefore - result.remainingAfter, rows = listOf(row))
    }

    private fun publish(outcome: X01TurnOutcome, scored: Int, rows: List<Throw>) {
        val winner = outcome.matchWonBy?.let { id -> players.first { it.id == id } }

        pointsScored[outcome.playerId] = (pointsScored[outcome.playerId] ?: 0) + scored
        dartsThrown[outcome.playerId] =
            (dartsThrown[outcome.playerId] ?: 0) + outcome.turnResult.dartsThrown

        _uiState.update {
            it.copy(
                boards = boards(),
                pendingDarts = emptyList(),
                previewRemaining = null,
                message = messageFor(outcome, scored),
                winner = winner
            )
        }

        viewModelScope.launch {
            withContext(NonCancellable) {
                writeLock.withLock {
                    rows.forEach { matchRepository.recordThrow(it) }
                    if (winner != null) {
                        match?.let { m ->
                            val completed = withFinalScore(m).copy(
                                winnerPlayerId = winner.id,
                                completedAt = System.currentTimeMillis()
                            )
                            matchRepository.updateMatch(completed)
                            match = completed
                        }
                    }
                }
            }
        }
    }

    /**
     * Snapshots the final score onto the match row, so the history screen can render a result
     * without replaying every throw. Scores stay parallel to `participantIds`; a participant the
     * controller does not know (deleted between matches) scores 0 rather than shifting the list.
     */
    private fun withFinalScore(match: Match): Match {
        val controller = controller ?: return match
        val playing = players.map { it.id }.toSet()
        return match.copy(
            setWins = match.participantIds.map { if (it in playing) controller.setWinsFor(it) else 0 },
            legWins = match.participantIds.map { if (it in playing) controller.totalLegWinsFor(it) else 0 }
        )
    }

    private fun messageFor(outcome: X01TurnOutcome, scored: Int): String {
        val name = nameOf(outcome.playerId)
        return when {
            outcome.matchWonBy != null -> "${nameOf(outcome.matchWonBy)} wins the match!"
            outcome.setWonBy != null -> "Set to ${nameOf(outcome.setWonBy)}"
            outcome.legWonBy != null -> "Leg to ${nameOf(outcome.legWonBy)}"
            outcome.turnResult.outcome == TurnResult.Outcome.BUST -> "Bust — $name scores nothing"
            !outcome.turnResult.isPlayerInAfter -> "$name is not in yet"
            else -> "$name scored $scored"
        }
    }

    private fun nextTurnNumber(playerId: Long): Int {
        val next = (turnNumbers[playerId] ?: 0) + 1
        turnNumbers[playerId] = next
        return next
    }

    private fun nameOf(playerId: Long): String =
        players.firstOrNull { it.id == playerId }?.name ?: "Player $playerId"

    private fun boards(): List<PlayerBoard> {
        val controller = controller ?: return emptyList()
        return players.map { player ->
            PlayerBoard(
                player = player,
                remaining = controller.remainingFor(player.id),
                legWins = controller.legWinsFor(player.id),
                setWins = controller.setWinsFor(player.id),
                isCurrentPlayer = controller.matchWinner == null && player.id == controller.currentPlayerId,
                isIn = controller.isInFor(player.id),
                threeDartAverage = threeDartAverageFor(player.id)
            )
        }
    }

    private fun threeDartAverageFor(playerId: Long): Double? {
        val darts = dartsThrown[playerId] ?: 0
        if (darts == 0) return null
        return (pointsScored[playerId] ?: 0) * 3.0 / darts
    }
}
