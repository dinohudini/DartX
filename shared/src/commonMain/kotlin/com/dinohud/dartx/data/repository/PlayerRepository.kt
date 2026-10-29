package com.dinohud.dartx.data.repository

import com.dinohud.dartx.data.local.Player
import com.dinohud.dartx.data.local.PlayerDao
import com.dinohud.dartx.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val playerDao: PlayerDao) {

    val activePlayers: Flow<List<Player>> = playerDao.getActivePlayers()

    val allPlayers: Flow<List<Player>> = playerDao.getAllPlayers()

    suspend fun addPlayer(player: Player): Long = playerDao.insert(player)

    suspend fun archivePlayer(player: Player) = playerDao.archive(player.id, currentTimeMillis())

    suspend fun getPlayerById(playerId: Long): Player? = playerDao.getPlayerById(playerId)
}
