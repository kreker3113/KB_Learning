package dev.kbwallet.app.watchlist.domain

import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    suspend fun addToWatchlist(coinId: String, coinName: String, coinSymbol: String, iconUrl: String, price: Double)
    suspend fun removeFromWatchlist(coinId: String)
    fun getWatchlistWithPrices(): Flow<List<WatchlistItem>>
    suspend fun isInWatchlist(coinId: String): Boolean
}
