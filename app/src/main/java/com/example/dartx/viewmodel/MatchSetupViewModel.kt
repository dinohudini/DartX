package com.example.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dartx.data.local.Match
import com.example.dartx.data.local.Player
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository
import com.example.dartx.model.GameMode
import com.example.dartx.model.InRule
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the setup flow. The match row is written before the live screen opens, so that screen
 * only needs a match id and reads every setting back from the repository.
 */
class MatchSetupViewModel(
    private val playerRepository: PlayerRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    val players: StateFlow<List<Player>> = playerRepository.allPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _createdMatchId = MutableStateFlow<Long?>(null)
    val createdMatchId: StateFlow<Long?> = _createdMatchId.asStateFlow()

    fun startMatch(
        participantIds: List<Long>,
        gameMode: GameMode,
        startPoints: Int,
        outRule: OutRule,
        inRule: InRule,
        setLegMode: SetLegMode,
        setsTarget: Int,
        legsTarget: Int
    ) {
        if (participantIds.size < 2) return
        viewModelScope.launch {
            val id = matchRepository.createMatch(
                Match(
                    gameMode = gameMode,
                    startPoints = startPoints,
                    outRule = outRule,
                    inRule = inRule,
                    setLegMode = setLegMode,
                    setsTarget = setsTarget,
                    legsTarget = legsTarget,
                    participantIds = participantIds
                )
            )
            _createdMatchId.value = id
        }
    }

    /** Clears the one-shot navigation signal so returning to setup doesn't re-open the match. */
    fun onMatchOpened() {
        _createdMatchId.value = null
    }
}
