package com.dinohud.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinohud.dartx.data.local.MatchWithParticipants
import com.dinohud.dartx.data.local.Player
import com.dinohud.dartx.data.local.Throw
import com.dinohud.dartx.data.repository.MatchRepository
import com.dinohud.dartx.data.repository.PlayerRepository
import com.dinohud.dartx.model.stats.PlayerOverallStats
import com.dinohud.dartx.model.stats.X01MatchStats
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.stat_leg_win_rate
import com.dinohud.dartx.resources.stat_legs_played
import com.dinohud.dartx.resources.stat_legs_won
import com.dinohud.dartx.resources.stat_matches_played
import com.dinohud.dartx.resources.stat_matches_won
import com.dinohud.dartx.resources.stat_section_record
import com.dinohud.dartx.resources.stat_win_rate
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
    matches: List<MatchWithParticipants>,
    throws: List<Throw>,
    players: List<Player>
): PlayerStatsUiState {
    val player = players.firstOrNull { it.id == playerId }
    val throwsByMatch = throws.groupBy { it.matchId }

    val perMatch = matches
        .filter { X01MatchStats.countsForStats(it.match) && playerId in it.playerIds }
        .mapNotNull { record ->
            X01MatchStats.compute(record, throwsByMatch[record.match.id].orEmpty())[playerId]
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
        title = Res.string.stat_section_record,
        items = listOf(
            statItem(Res.string.stat_matches_played, overall.matchesPlayed.toString()),
            statItem(Res.string.stat_matches_won, overall.matchesWon.toString()),
            statItem(Res.string.stat_win_rate, overall.winRate.orNoValue(::formatRate)),
            statItem(Res.string.stat_legs_played, overall.legsPlayed.toString()),
            statItem(Res.string.stat_legs_won, overall.legsWon.toString()),
            statItem(Res.string.stat_leg_win_rate, overall.legWinRate.orNoValue(::formatRate))
        )
    )
)
