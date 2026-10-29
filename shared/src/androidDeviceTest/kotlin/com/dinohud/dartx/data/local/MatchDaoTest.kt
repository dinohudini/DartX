package com.dinohud.dartx.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.SQLiteException
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dinohud.dartx.model.GameMode
import com.dinohud.dartx.model.InRule
import com.dinohud.dartx.model.Multiplier
import com.dinohud.dartx.model.OutRule
import com.dinohud.dartx.model.SetLegMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatchDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var matchDao: MatchDao
    private lateinit var throwDao: ThrowDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder<AppDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        playerDao = database.playerDao()
        matchDao = database.matchDao()
        throwDao = database.throwDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun match() = Match(
        gameMode = GameMode.X01,
        startPoints = 501,
        outRule = OutRule.DOUBLE_OUT,
        inRule = InRule.STRAIGHT_IN,
        setLegMode = SetLegMode.FIRST_TO,
        setsTarget = 1,
        legsTarget = 3
    )

    private fun dart(matchId: Long, playerId: Long, turn: Int, index: Int, value: Int) = Throw(
        matchId = matchId,
        playerId = playerId,
        turnNumber = turn,
        dartIndex = index,
        fieldValue = value,
        multiplier = Multiplier.SINGLE,
        score = value
    )

    private suspend fun players(vararg names: String): List<Long> =
        names.map { playerDao.insert(Player(name = it, avatarColor = "#F2795E")) }

    @Test
    fun participantsKeepTheirSeatOrder() = runBlocking {
        val (ana, marko, ivana) = players("Ana", "Marko", "Ivana")

        val matchId = matchDao.insertWithParticipants(match(), listOf(ivana, ana, marko))

        val record = matchDao.getMatchById(matchId)!!
        assertEquals(listOf(ivana, ana, marko), record.playerIds)
        assertEquals(listOf(0, 1, 2), record.seats.map { it.seat })
    }

    @Test
    fun saveResultWritesTheMatchAndEveryParticipant() = runBlocking {
        val (ana, marko) = players("Ana", "Marko")
        val matchId = matchDao.insertWithParticipants(match(), listOf(ana, marko))
        val record = matchDao.getMatchById(matchId)!!

        matchDao.updateWithParticipants(
            record.match.copy(winnerPlayerId = marko, completedAt = 2_000L),
            record.seats.map { it.copy(legWins = if (it.playerId == marko) 3 else 1) }
        )

        val saved = matchDao.observeMatchById(matchId).first()!!
        assertEquals(marko, saved.match.winnerPlayerId)
        assertEquals(listOf(1, 3), saved.seats.map { it.legWins })
    }

    @Test
    fun throwsComeBackInDartOrderWhateverTheInsertOrder() = runBlocking {
        val (ana, marko) = players("Ana", "Marko")
        val matchId = matchDao.insertWithParticipants(match(), listOf(ana, marko))

        throwDao.insert(dart(matchId, ana, turn = 1, index = 2, value = 3))
        throwDao.insert(dart(matchId, ana, turn = 1, index = 0, value = 1))
        throwDao.insert(dart(matchId, ana, turn = 1, index = 1, value = 2))

        assertEquals(listOf(1, 2, 3), throwDao.getThrowsForMatch(matchId).first().map { it.score })
        assertEquals(listOf(1, 2, 3), throwDao.getThrowsForPlayer(ana).first().map { it.score })
    }

    @Test
    fun theSameDartCannotBeRecordedTwice() {
        runBlocking {
            val (ana, marko) = players("Ana", "Marko")
            val matchId = matchDao.insertWithParticipants(match(), listOf(ana, marko))
            throwDao.insert(dart(matchId, ana, turn = 1, index = 0, value = 20))

            assertThrows(SQLiteException::class.java) {
                runBlocking { throwDao.insert(dart(matchId, ana, turn = 1, index = 0, value = 5)) }
            }
        }
    }

    @Test
    fun throwsMustBelongToAnExistingMatchAndPlayer() {
        runBlocking {
            val (ana) = players("Ana")

            assertThrows(SQLiteException::class.java) {
                runBlocking { throwDao.insert(dart(matchId = 999L, playerId = ana, turn = 1, index = 0, value = 20)) }
            }
        }
    }

    @Test
    fun aMatchWithAMissingPlayerIsRejectedWhole() {
        runBlocking {
            val (ana) = players("Ana")

            assertThrows(SQLiteException::class.java) {
                runBlocking { matchDao.insertWithParticipants(match(), listOf(ana, 999L)) }
            }
            assertNull(matchDao.getAllMatches().first().firstOrNull())
        }
    }
}
