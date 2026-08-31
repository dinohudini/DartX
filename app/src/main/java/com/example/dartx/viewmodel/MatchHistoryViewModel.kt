package com.example.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dartx.data.local.Match
import com.example.dartx.data.local.Player
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryParticipant(
    val playerId: Long,
    val name: String,
    val avatarColor: String?,
    val setWins: Int?,
    val legWins: Int?,
    val isWinner: Boolean
)

data class MatchHistoryEntry(
    val match: Match,
    val participants: List<HistoryParticipant>
) {
    val isFinished: Boolean get() = match.winnerPlayerId != null
}

data class MatchHistoryUiState(
    val isLoading: Boolean = true,
    val entries: List<MatchHistoryEntry> = emptyList()
)

class MatchHistoryViewModel(
    matchRepository: MatchRepository,
    playerRepository: PlayerRepository
) : ViewModel() {

    val uiState: StateFlow<MatchHistoryUiState> =
        combine(matchRepository.allMatches, playerRepository.allPlayers) { matches, players ->
            val byId = players.associateBy(Player::id)
            matches.map { match -> toEntry(match, byId) }
        }
            .map { MatchHistoryUiState(isLoading = false, entries = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MatchHistoryUiState()
            )
}

private fun toEntry(match: Match, playersById: Map<Long, Player>): MatchHistoryEntry {
    val participants = match.participantIds.mapIndexed { index, playerId ->
        val player = playersById[playerId]
        HistoryParticipant(
            playerId = playerId,
            name = player?.name ?: "Deleted player",
            avatarColor = player?.avatarColor,
            setWins = match.setWins.getOrNull(index),
            legWins = match.legWins.getOrNull(index),
            isWinner = playerId == match.winnerPlayerId
        )
    }
    return MatchHistoryEntry(match = match, participants = participants)
}
