package com.dinohud.dartx.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation

@Entity(
    tableName = "match_participants",
    primaryKeys = ["matchId", "seat"],
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
        Index(value = ["matchId", "playerId"], unique = true),
        Index(value = ["playerId"])
    ]
)
data class MatchParticipant(
    val matchId: Long,
    val playerId: Long,
    val seat: Int,
    val setWins: Int = 0,
    val legWins: Int = 0
)

data class MatchWithParticipants(
    @Embedded val match: Match,
    @Relation(parentColumn = "id", entityColumn = "matchId")
    val participants: List<MatchParticipant>
) {
    val seats: List<MatchParticipant>
        get() = participants.sortedBy { it.seat }

    val playerIds: List<Long>
        get() = seats.map { it.playerId }
}
