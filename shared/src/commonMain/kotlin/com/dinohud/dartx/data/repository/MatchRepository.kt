package com.dinohud.dartx.data.repository

import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.data.local.MatchDao
import com.dinohud.dartx.data.local.MatchParticipant
import com.dinohud.dartx.data.local.MatchWithParticipants
import com.dinohud.dartx.data.local.Throw
import com.dinohud.dartx.data.local.ThrowDao
import kotlinx.coroutines.flow.Flow

class MatchRepository(
    private val matchDao: MatchDao,
    private val throwDao: ThrowDao
) {

    val allMatches: Flow<List<MatchWithParticipants>> = matchDao.getAllMatches()

    suspend fun createMatch(match: Match, playerIds: List<Long>): Long =
        matchDao.insertWithParticipants(match, playerIds)

    suspend fun saveResult(match: Match, participants: List<MatchParticipant>) =
        matchDao.updateWithParticipants(match, participants)

    suspend fun getMatchById(matchId: Long): MatchWithParticipants? = matchDao.getMatchById(matchId)

    fun observeMatchById(matchId: Long): Flow<MatchWithParticipants?> = matchDao.observeMatchById(matchId)

    suspend fun recordThrow(throwEntry: Throw): Long = throwDao.insert(throwEntry)

    suspend fun deleteTurn(matchId: Long, playerId: Long, turnNumber: Int) =
        throwDao.deleteTurn(matchId, playerId, turnNumber)

    fun getThrowsForMatch(matchId: Long): Flow<List<Throw>> = throwDao.getThrowsForMatch(matchId)

    fun getThrowsForPlayer(playerId: Long): Flow<List<Throw>> = throwDao.getThrowsForPlayer(playerId)
}
