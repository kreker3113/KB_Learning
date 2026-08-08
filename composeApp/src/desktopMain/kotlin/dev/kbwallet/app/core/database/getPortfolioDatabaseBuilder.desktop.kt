package dev.kbwallet.app.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.kbwallet.app.core.database.portfolio.PortfolioDatabase
import java.io.File

fun getPortfolioDatabaseBuilder(): RoomDatabase.Builder<PortfolioDatabase> {
    val appDataDir = File(System.getProperty("user.home"), ".kbwallet").apply { mkdirs() }
    val dbFile = File(appDataDir, "portfolio.db")
    return Room.databaseBuilder<PortfolioDatabase>(
        name = dbFile.absolutePath,
    )
}
