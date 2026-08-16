package dev.kbwallet.app.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

// The on-disk file name stays "portfolio.db" even after AppDatabase's rename
// (see AppDatabase.kt doc comment) — changing it would orphan every existing
// install's local data on update.
fun getAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appDataDir = File(System.getProperty("user.home"), ".kbwallet").apply { mkdirs() }
    val dbFile = File(appDataDir, "portfolio.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
