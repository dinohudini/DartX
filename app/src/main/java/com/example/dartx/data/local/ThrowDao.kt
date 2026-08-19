package com.example.dartx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThrowDao {

    @Insert
    suspend fun insert(throwEntry: Throw): Long

    @Query(
        "DELETE FROM throws WHERE matchId = :matchId AND playerId = :playerId AND turnNumber = :turnNumber"
    )
    suspend fun deleteTurn(matchId: Long, playerId: Long, turnNumber: Int)

    @Query("SELECT * FROM throws WHERE matchId = :matchId ORDER BY turnNumber ASC, id ASC")
    fun getThrowsForMatch(matchId: Long): Flow<List<Throw>>

    @Query("SELECT * FROM throws WHERE playerId = :playerId ORDER BY matchId ASC, turnNumber ASC, id ASC")
    fun getThrowsForPlayer(playerId: Long): Flow<List<Throw>>
}
