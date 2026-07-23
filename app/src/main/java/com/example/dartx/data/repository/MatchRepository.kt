package com.example.dartx.data.repository

import com.example.dartx.data.local.Match
import com.example.dartx.data.local.MatchDao
import com.example.dartx.data.local.Throw
import com.example.dartx.data.local.ThrowDao
import kotlinx.coroutines.flow.Flow

class MatchRepository(
    private val matchDao: MatchDao,
    private val throwDao: ThrowDao
) {

    val allMatches: Flow<List<Match>> = matchDao.getAllMatches()

    suspend fun createMatch(match: Match): Long = matchDao.insert(match)

    suspend fun updateMatch(match: Match) = matchDao.update(match)

    suspend fun getMatchById(matchId: Long): Match? = matchDao.getMatchById(matchId)

    suspend fun recordThrow(throwEntry: Throw): Long = throwDao.insert(throwEntry)

    fun getThrowsForMatch(matchId: Long): Flow<List<Throw>> = throwDao.getThrowsForMatch(matchId)

    fun getThrowsForPlayer(playerId: Long): Flow<List<Throw>> = throwDao.getThrowsForPlayer(playerId)
}
