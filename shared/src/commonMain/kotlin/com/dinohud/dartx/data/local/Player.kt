package com.dinohud.dartx.data.local

import com.dinohud.dartx.util.currentTimeMillis
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarColor: String,
    val createdAt: Long = currentTimeMillis(),
    val archivedAt: Long? = null
)
