package com.dinohud.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.data.local.Player
import com.dinohud.dartx.data.local.Throw
import com.dinohud.dartx.data.repository.MatchRepository
import com.dinohud.dartx.data.repository.PlayerRepository
import com.dinohud.dartx.model.stats.PlayerMatchStats
import com.dinohud.dartx.model.stats.X01MatchStats
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.stat_legs_won
import com.dinohud.dartx.resources.stat_outcome
import com.dinohud.dartx.resources.stat_outcome_lost
import com.dinohud.dartx.resources.stat_outcome_won
import com.dinohud.dartx.resources.stat_section_result
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PlayerStatsBlock(
    val playerId: Long,
    val name: String?,
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
                name = player?.name,
                avatarColor = player?.avatarColor,
                sections = stats.getValue(playerId).let { resultSection(it) + scoringSections(it.totals) }
            )
        }
    )
}

private fun resultSection(stats: PlayerMatchStats): List<StatSection> = listOf(
    StatSection(
        title = Res.string.stat_section_result,
        items = listOf(
            StatItem(
                uiText(Res.string.stat_outcome),
                uiText(if (stats.isWinner) Res.string.stat_outcome_won else Res.string.stat_outcome_lost)
            ),
            statItem(Res.string.stat_legs_won, "${stats.legsWon} / ${stats.legsPlayed}")
        )
    )
)
