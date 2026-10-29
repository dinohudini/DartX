package com.dinohud.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.data.local.MatchWithParticipants
import com.dinohud.dartx.data.local.Player
import com.dinohud.dartx.data.repository.MatchRepository
import com.dinohud.dartx.data.repository.PlayerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryParticipant(
    val playerId: Long,
    val name: String?,
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

private fun toEntry(record: MatchWithParticipants, playersById: Map<Long, Player>): MatchHistoryEntry {
    val match = record.match
    val isFinished = match.winnerPlayerId != null
    val participants = record.seats.map { participant ->
        val player = playersById[participant.playerId]
        HistoryParticipant(
            playerId = participant.playerId,
            name = player?.name,
            avatarColor = player?.avatarColor,
            setWins = participant.setWins.takeIf { isFinished },
            legWins = participant.legWins.takeIf { isFinished },
            isWinner = participant.playerId == match.winnerPlayerId
        )
    }
    return MatchHistoryEntry(match = match, participants = participants)
}
