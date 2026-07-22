package com.example.dartx.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarColor: String,
    val createdAt: Long = System.currentTimeMillis()
)
