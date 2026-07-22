package com.example.dartx.data.repository

import com.example.dartx.data.local.Player
import com.example.dartx.data.local.PlayerDao
import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val playerDao: PlayerDao) {

    val allPlayers: Flow<List<Player>> = playerDao.getAllPlayers()

    suspend fun addPlayer(player: Player): Long = playerDao.insert(player)

    suspend fun deletePlayer(player: Player) = playerDao.delete(player)

    suspend fun getPlayerById(playerId: Long): Player? = playerDao.getPlayerById(playerId)
}
