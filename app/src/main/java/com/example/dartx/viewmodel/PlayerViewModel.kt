package com.example.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dartx.data.local.Player
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository
import com.example.dartx.model.stats.X01MatchStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerRow(
    val player: Player,
    val matchesPlayed: Int,
    val matchesWon: Int
)

class PlayerViewModel(
    private val repository: PlayerRepository,
    matchRepository: MatchRepository
) : ViewModel() {

    val players: StateFlow<List<Player>> = repository.allPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rows: StateFlow<List<PlayerRow>> =
        combine(repository.allPlayers, matchRepository.allMatches) { players, matches ->
            val counted = matches.filter(X01MatchStats::countsForStats)
            players.map { player ->
                PlayerRow(
                    player = player,
                    matchesPlayed = counted.count { player.id in it.participantIds },
                    matchesWon = counted.count { it.winnerPlayerId == player.id }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlayer(name: String, avatarColor: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            repository.addPlayer(Player(name = trimmedName, avatarColor = avatarColor))
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repository.deletePlayer(player)
        }
    }
}
