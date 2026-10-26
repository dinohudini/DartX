package com.dinohud.dartx.data.local

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers

fun createDartXDatabase(context: Context): AppDatabase {
    val appContext = context.applicationContext
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(AppDatabase.DATABASE_NAME).absolutePath
    ).buildDartXDatabase(Dispatchers.IO)
}
