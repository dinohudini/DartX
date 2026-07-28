package com.example.dartx.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dartx.model.GameMode
import com.example.dartx.model.InRule
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode

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
    /**
     * Final score, written once when the match is won, and empty until then — both lists are
     * parallel to [participantIds]. [legWins] counts legs over the whole match, not just the
     * last set, because per-set leg counts reset every time a set is clinched.
     */
    val setWins: List<Int> = emptyList(),
    val legWins: List<Int> = emptyList(),
    val winnerPlayerId: Long? = null,
    val tournamentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
