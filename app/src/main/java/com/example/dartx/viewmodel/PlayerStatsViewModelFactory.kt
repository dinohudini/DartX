package com.example.dartx.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dartx.data.local.AppDatabase
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository

class PlayerStatsViewModelFactory(
    private val context: Context,
    private val playerId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getInstance(context)
        return PlayerStatsViewModel(
            playerId,
            MatchRepository(database.matchDao(), database.throwDao()),
            PlayerRepository(database.playerDao())
        ) as T
    }
}
