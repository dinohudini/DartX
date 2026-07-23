package com.example.dartx.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dartx.model.Multiplier

/**
 * One dart, or (for fast turn-total entry) one whole turn recorded as a single
 * generic row with [fieldValue]/[multiplier] left null and [score] holding the total.
 */
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
    val timestamp: Long = System.currentTimeMillis()
)
