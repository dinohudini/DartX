package com.example.dartx.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dartx.model.Multiplier
import com.example.dartx.model.ThrowStatus

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
    val timestamp: Long = System.currentTimeMillis()
)
