package dev.kbwallet.app.core.database.portfolio

import androidx.room.RoomDatabaseConstructor

public actual object PortfolioDatabaseCreator : RoomDatabaseConstructor<PortfolioDatabase> {
  override fun initialize(): PortfolioDatabase =
      dev.kbwallet.app.core.database.portfolio.PortfolioDatabase_Impl()
}
