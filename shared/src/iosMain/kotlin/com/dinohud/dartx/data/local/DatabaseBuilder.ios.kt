package com.dinohud.dartx.data.local

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun createDartXDatabase(): AppDatabase =
    Room.databaseBuilder<AppDatabase>(name = "${documentsDirectory()}/${AppDatabase.DATABASE_NAME}")
        .buildDartXDatabase(Dispatchers.IO)

@OptIn(ExperimentalForeignApi::class)
private fun documentsDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(url?.path)
}
