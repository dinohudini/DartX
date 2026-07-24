package com.example.dartx.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dartx.data.local.AppDatabase
import com.example.dartx.data.repository.MatchRepository
import com.example.dartx.data.repository.PlayerRepository

class MatchSetupViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getInstance(context)
        return MatchSetupViewModel(
            PlayerRepository(database.playerDao()),
            MatchRepository(database.matchDao(), database.throwDao())
        ) as T
    }
}
