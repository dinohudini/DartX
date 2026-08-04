package com.example.dartx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: Match): Long

    @Update
    suspend fun update(match: Match)

    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun getAllMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): Match?

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun observeMatchById(matchId: Long): Flow<Match?>
}
