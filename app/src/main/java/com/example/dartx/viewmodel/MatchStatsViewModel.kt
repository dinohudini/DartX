package com.example.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dartx.data.local.Match
import com.example.dartx.data.local.Player
import com.example.dartx.data.local.Throw
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository
import com.example.dartx.model.stats.PlayerMatchStats
import com.example.dartx.model.stats.X01MatchStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PlayerStatsBlock(
    val playerId: Long,
    val name: String,
    val avatarColor: String?,
    val sections: List<StatSection>
)

data class MatchStatsUiState(
    val isLoading: Boolean = true,
    val match: Match? = null,
    val isCounted: Boolean = false,
    val players: List<PlayerStatsBlock> = emptyList()
)

class MatchStatsViewModel(
    matchId: Long,
    matchRepository: MatchRepository,
    playerRepository: PlayerRepository
) : ViewModel() {

    val uiState: StateFlow<MatchStatsUiState> = combine(
        matchRepository.observeMatchById(matchId),
        matchRepository.getThrowsForMatch(matchId),
        playerRepository.allPlayers
    ) { match, throws, players -> toUiState(match, throws, players) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MatchStatsUiState()
        )
}

private fun toUiState(match: Match?, throws: List<Throw>, players: List<Player>): MatchStatsUiState {
    if (match == null) return MatchStatsUiState(isLoading = false)
    if (!X01MatchStats.countsForStats(match)) {
        return MatchStatsUiState(isLoading = false, match = match)
    }

    val playersById = players.associateBy(Player::id)
    val stats = X01MatchStats.compute(match, throws)

    return MatchStatsUiState(
        isLoading = false,
        match = match,
        isCounted = true,
        players = match.participantIds.map { playerId ->
            val player = playersById[playerId]
            PlayerStatsBlock(
                playerId = playerId,
                name = player?.name ?: "Deleted player",
                avatarColor = player?.avatarColor,
                sections = stats.getValue(playerId).let { resultSection(it) + scoringSections(it.totals) }
            )
        }
    )
}

private fun resultSection(stats: PlayerMatchStats): List<StatSection> = listOf(
    StatSection(
        title = "Result",
        items = listOf(
            StatItem("Outcome", if (stats.isWinner) "Won" else "Lost"),
            StatItem("Legs won", "${stats.legsWon} / ${stats.legsPlayed}")
        )
    )
)
