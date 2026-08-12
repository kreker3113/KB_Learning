package dev.kbwallet.app.watchlist.domain

import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    suspend fun addToWatchlist(coinId: String, coinName: String, coinSymbol: String, iconUrl: String, price: Double)
    suspend fun removeFromWatchlist(coinId: String)
    fun getWatchlistWithPrices(): Flow<Result<List<WatchlistItem>, DataError.Remote>>
    suspend fun isInWatchlist(coinId: String): Boolean
}
