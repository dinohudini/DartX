package com.dinohud.dartx.data.local

import com.dinohud.dartx.util.currentTimeMillis
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dinohud.dartx.model.Multiplier
import com.dinohud.dartx.model.ThrowStatus

@Entity(tableName = "throws")
data class Throw(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: Long,
    val playerId: Long,
    val turnNumber: Int,
    val fieldValue: Int?,
    val multiplier: Multiplier?,
    val score: Int,
    val legNumber: Int = 1,
    val status: ThrowStatus = ThrowStatus.COUNTED,
    val timestamp: Long = currentTimeMillis()
)
