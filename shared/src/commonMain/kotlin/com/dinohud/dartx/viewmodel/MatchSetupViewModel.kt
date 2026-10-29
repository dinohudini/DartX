package com.dinohud.dartx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.data.local.Player
import com.dinohud.dartx.data.repository.MatchRepository
import com.dinohud.dartx.data.repository.PlayerRepository
import com.dinohud.dartx.model.GameMode
import com.dinohud.dartx.model.InRule
import com.dinohud.dartx.model.OutRule
import com.dinohud.dartx.model.SetLegMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MatchSetupViewModel(
    private val playerRepository: PlayerRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    val players: StateFlow<List<Player>> = playerRepository.activePlayers
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
                    legsTarget = legsTarget
                ),
                participantIds
            )
            _createdMatchId.value = id
        }
    }

    fun onMatchOpened() {
        _createdMatchId.value = null
    }
}
