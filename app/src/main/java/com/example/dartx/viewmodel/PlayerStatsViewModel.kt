package com.example.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dartx.data.local.Match
import com.example.dartx.data.local.Player
import com.example.dartx.data.local.Throw
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository
import com.example.dartx.model.stats.PlayerOverallStats
import com.example.dartx.model.stats.X01MatchStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PlayerStatsUiState(
    val isLoading: Boolean = true,
    val name: String? = null,
    val avatarColor: String? = null,
    val hasPlayed: Boolean = false,
    val sections: List<StatSection> = emptyList()
)

class PlayerStatsViewModel(
    private val playerId: Long,
    matchRepository: MatchRepository,
    playerRepository: PlayerRepository
) : ViewModel() {

    val uiState: StateFlow<PlayerStatsUiState> = combine(
        matchRepository.allMatches,
        matchRepository.getThrowsForPlayer(playerId),
        playerRepository.allPlayers
    ) { matches, throws, players -> toUiState(playerId, matches, throws, players) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerStatsUiState()
        )
}

private fun toUiState(
    playerId: Long,
    matches: List<Match>,
    throws: List<Throw>,
    players: List<Player>
): PlayerStatsUiState {
    val player = players.firstOrNull { it.id == playerId }
    val throwsByMatch = throws.groupBy { it.matchId }

    val perMatch = matches
        .filter { X01MatchStats.countsForStats(it) && playerId in it.participantIds }
        .mapNotNull { match ->
            X01MatchStats.compute(match, throwsByMatch[match.id].orEmpty())[playerId]
        }

    val overall = PlayerOverallStats.aggregate(playerId, perMatch)

    return PlayerStatsUiState(
        isLoading = false,
        name = player?.name,
        avatarColor = player?.avatarColor,
        hasPlayed = overall.matchesPlayed > 0,
        sections = recordSection(overall) + scoringSections(overall.totals)
    )
}

private fun recordSection(overall: PlayerOverallStats): List<StatSection> = listOf(
    StatSection(
        title = "Record",
        items = listOf(
            StatItem("Matches played", overall.matchesPlayed.toString()),
            StatItem("Matches won", overall.matchesWon.toString()),
            StatItem("Win rate", overall.winRate?.let(::formatRate) ?: "—"),
            StatItem("Legs played", overall.legsPlayed.toString()),
            StatItem("Legs won", overall.legsWon.toString()),
            StatItem("Leg win rate", overall.legWinRate?.let(::formatRate) ?: "—")
        )
    )
)
