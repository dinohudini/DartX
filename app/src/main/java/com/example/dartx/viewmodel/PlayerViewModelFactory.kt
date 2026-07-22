package com.example.dartx.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dartx.data.local.AppDatabase
import com.example.dartx.data.repository.PlayerRepository

class PlayerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val playerDao = AppDatabase.getInstance(context).playerDao()
        return PlayerViewModel(PlayerRepository(playerDao)) as T
    }
}
