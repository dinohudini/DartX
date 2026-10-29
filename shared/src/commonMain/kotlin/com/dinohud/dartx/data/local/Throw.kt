package com.dinohud.dartx.data.local

import com.dinohud.dartx.util.currentTimeMillis
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinohud.dartx.model.Multiplier
import com.dinohud.dartx.model.ThrowStatus

@Entity(
    tableName = "throws",
    foreignKeys = [
        ForeignKey(
            entity = Match::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["id"],
            childColumns = ["playerId"]
        )
    ],
    indices = [
        Index(value = ["matchId", "playerId", "turnNumber", "dartIndex"], unique = true),
        Index(value = ["playerId"])
    ]
)
data class Throw(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: Long,
    val playerId: Long,
    val turnNumber: Int,
    val dartIndex: Int,
    val fieldValue: Int?,
    val multiplier: Multiplier?,
    val score: Int,
    val legNumber: Int = 1,
    val status: ThrowStatus = ThrowStatus.COUNTED,
    val timestamp: Long = currentTimeMillis()
)
