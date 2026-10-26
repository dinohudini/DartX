package com.dinohud.dartx.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.coroutines.CoroutineContext

@Database(
    entities = [Player::class, Match::class, Throw::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun throwDao(): ThrowDao

    companion object {
        const val DATABASE_NAME = "dartx_database"
    }
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun RoomDatabase.Builder<AppDatabase>.buildDartXDatabase(queryContext: CoroutineContext): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryContext)
        .build()
