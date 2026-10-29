package com.dinohud.dartx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert
    suspend fun insert(player: Player): Long

    @Query("UPDATE players SET archivedAt = :archivedAt WHERE id = :playerId")
    suspend fun archive(playerId: Long, archivedAt: Long)

    @Query("SELECT * FROM players WHERE archivedAt IS NULL ORDER BY name ASC")
    fun getActivePlayers(): Flow<List<Player>>

    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayers(): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Long): Player?
}
