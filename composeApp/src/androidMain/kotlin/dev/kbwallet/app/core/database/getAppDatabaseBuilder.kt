package dev.kbwallet.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

// The on-disk file name stays "portfolio.db" even after AppDatabase's rename
// (see AppDatabase.kt doc comment) — changing it would orphan every existing
// install's local data on update.
fun getAppDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("portfolio.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath,
        )
}
