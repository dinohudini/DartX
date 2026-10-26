package com.dinohud.dartx.data.local

import com.dinohud.dartx.util.currentTimeMillis
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dinohud.dartx.model.GameMode
import com.dinohud.dartx.model.InRule
import com.dinohud.dartx.model.OutRule
import com.dinohud.dartx.model.SetLegMode

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameMode: GameMode,
    val startPoints: Int,
    val outRule: OutRule,
    val inRule: InRule,
    val setLegMode: SetLegMode,
    val setsTarget: Int,
    val legsTarget: Int,
    val participantIds: List<Long>,
    val setWins: List<Int> = emptyList(),
    val legWins: List<Int> = emptyList(),
    val winnerPlayerId: Long? = null,
    val tournamentId: Long? = null,
    val createdAt: Long = currentTimeMillis(),
    val completedAt: Long? = null
)
