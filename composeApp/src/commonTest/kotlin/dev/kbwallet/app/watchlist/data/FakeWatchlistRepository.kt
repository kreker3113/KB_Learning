package dev.kbwallet.app.watchlist.data

import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.watchlist.domain.WatchlistItem
import dev.kbwallet.app.watchlist.domain.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWatchlistRepository : WatchlistRepository {

    private val watchlist = mutableListOf<WatchlistItem>()
    private val watchlistFlow = MutableStateFlow<List<WatchlistItem>>(emptyList())
    
    var simulateError = false

    override suspend fun addToWatchlist(
        coinId: String,
        coinName: String,
        coinSymbol: String,
        iconUrl: String,
        price: Double
    ) {
        val item = WatchlistItem(
            coin = dev.kbwallet.app.core.domain.coin.Coin(coinId, coinName, coinSymbol, iconUrl),
            currentPrice = price,
            change24h = 0.0,
            addedPrice = price,
            addedAt = 0L
        )
        watchlist.add(item)
        watchlistFlow.value = watchlist.toList()
    }

    override suspend fun removeFromWatchlist(coinId: String) {
        watchlist.removeAll { it.coin.id == coinId }
        watchlistFlow.value = watchlist.toList()
    }

    override fun getWatchlistWithPrices(): Flow<Result<List<WatchlistItem>, DataError.Remote>> {
        return watchlistFlow.map { 
            if (simulateError) Result.Error(DataError.Remote.SERVER)
            else Result.Success(it)
        }
    }

    override suspend fun isInWatchlist(coinId: String): Boolean {
        return watchlist.any { it.coin.id == coinId }
    }
}
