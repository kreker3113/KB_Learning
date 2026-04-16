package dev.kbwallet.app.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class PortfolioCoinEntity(
    @PrimaryKey val coinId: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val averagePurchasePrice: Double,
    val amountOwned: Double,
    val timestamp: Long,
)