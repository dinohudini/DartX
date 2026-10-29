package com.dinohud.dartx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: Match): Long

    @Insert
    suspend fun insertParticipants(participants: List<MatchParticipant>)

    @Update
    suspend fun update(match: Match)

    @Update
    suspend fun updateParticipants(participants: List<MatchParticipant>)

    @Transaction
    suspend fun insertWithParticipants(match: Match, playerIds: List<Long>): Long {
        val matchId = insert(match)
        insertParticipants(
            playerIds.mapIndexed { seat, playerId ->
                MatchParticipant(matchId = matchId, playerId = playerId, seat = seat)
            }
        )
        return matchId
    }

    @Transaction
    suspend fun updateWithParticipants(match: Match, participants: List<MatchParticipant>) {
        update(match)
        updateParticipants(participants)
    }

    @Transaction
    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun getAllMatches(): Flow<List<MatchWithParticipants>>

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchWithParticipants?

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun observeMatchById(matchId: Long): Flow<MatchWithParticipants?>
}
