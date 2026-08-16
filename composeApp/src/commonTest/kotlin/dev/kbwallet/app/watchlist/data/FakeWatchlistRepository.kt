package dev.kbwallet.app.watchlist.data

import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.watchlist.domain.WatchlistItem
import dev.kbwallet.app.watchlist.domain.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Hand-written fake — see FakeCoinsRemoteDataSource for the project's testing convention. */
class FakeWatchlistRepository : WatchlistRepository {

    private val _items = MutableStateFlow<List<WatchlistItem>>(emptyList())

    override suspend fun addToWatchlist(
        coinId: String,
        coinName: String,
        coinSymbol: String,
        iconUrl: String,
        price: Double,
    ) {
        _items.update {
            it + WatchlistItem(
                coin = Coin(id = coinId, name = coinName, symbol = coinSymbol, iconUrl = iconUrl),
                currentPrice = price,
                change24h = 0.0,
                addedPrice = price,
                addedAt = 0L,
            )
        }
    }

    override suspend fun removeFromWatchlist(coinId: String) {
        _items.update { list -> list.filterNot { it.coin.id == coinId } }
    }

    override fun getWatchlistWithPrices(): Flow<List<WatchlistItem>> = _items.asStateFlow()

    override suspend fun isInWatchlist(coinId: String): Boolean = _items.value.any { it.coin.id == coinId }

    /** Test-only shortcut to seed an item without going through addToWatchlist's defaults. */
    fun seed(item: WatchlistItem) {
        _items.update { it + item }
    }

    companion object {
        val fakeItem = WatchlistItem(
            coin = Coin(id = "bitcoin", name = "Bitcoin", symbol = "BTC", iconUrl = "https://fake.url/btc.png"),
            currentPrice = 65000.0,
            change24h = 2.5,
            addedPrice = 60000.0,
            addedAt = 0L,
        )
    }
}
