package com.example.dartx

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dartx.data.local.AppDatabase
import com.example.dartx.data.local.Player
import com.example.dartx.data.local.PlayerDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var playerDao: PlayerDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playerDao = database.playerDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertPlayer_appearsInAllPlayers() = runBlocking {
        val player = Player(name = "Marko", avatarColor = "#FF5733")

        playerDao.insert(player)

        val players = playerDao.getAllPlayers().first()
        assertEquals(1, players.size)
        assertEquals("Marko", players[0].name)
    }

    @Test
    fun deletePlayer_removesFromAllPlayers() = runBlocking {
        val player = Player(name = "Ivana", avatarColor = "#33A1FF")
        val id = playerDao.insert(player)
        val inserted = playerDao.getPlayerById(id)!!

        playerDao.delete(inserted)

        val players = playerDao.getAllPlayers().first()
        assertTrue(players.isEmpty())
    }

    @Test
    fun getPlayerById_returnsCorrectPlayer() = runBlocking {
        val player = Player(name = "Ana", avatarColor = "#00FF00")
        val id = playerDao.insert(player)

        val fetched = playerDao.getPlayerById(id)

        assertEquals("Ana", fetched?.name)
        assertEquals("#00FF00", fetched?.avatarColor)
    }
}
