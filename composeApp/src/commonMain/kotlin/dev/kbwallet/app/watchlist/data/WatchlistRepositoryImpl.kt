package dev.kbwallet.app.watchlist.data

import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.watchlist.domain.WatchlistItem
import dev.kbwallet.app.watchlist.domain.WatchlistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class WatchlistRepositoryImpl(
    private val watchlistDao: WatchlistDao,
    private val coinsRemoteDataSource: CoinsRemoteDataSource,
) : WatchlistRepository {

    override suspend fun addToWatchlist(
        coinId: String, coinName: String, coinSymbol: String, iconUrl: String, price: Double,
    ) {
        watchlistDao.addToWatchlist(
            WatchlistEntity(
                coinId = coinId,
                coinName = coinName,
                coinSymbol = coinSymbol,
                iconUrl = iconUrl,
                addedPrice = price,
                addedAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    override suspend fun removeFromWatchlist(coinId: String) {
        watchlistDao.removeFromWatchlist(coinId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getWatchlistWithPrices(): Flow<List<WatchlistItem>> {
        return watchlistDao.getAllWatchlistItems().flatMapLatest { entities ->
            if (entities.isEmpty()) {
                flow { emit(emptyList()) }
            } else {
                flow {
                    val result = coinsRemoteDataSource.getListOfCoins()
                    val items = when {
                        result is dev.kbwallet.app.core.domain.Result.Success -> {
                            entities.mapNotNull { entity ->
                                val coin = result.data.data.coins.find { it.uuid == entity.coinId }
                                coin?.let {
                                    WatchlistItem(
                                        coin = dev.kbwallet.app.core.domain.coin.Coin(
                                            id = it.uuid,
                                            name = it.name,
                                            symbol = it.symbol,
                                            iconUrl = it.iconUrl,
                                        ),
                                        currentPrice = it.price,
                                        change24h = it.change,
                                        addedPrice = entity.addedPrice,
                                        addedAt = entity.addedAt,
                                    )
                                }
                            }
                        }
                        else -> emptyList()
                    }
                    emit(items)
                }
            }
        }.catch {
            emit(emptyList())
        }
    }

    override suspend fun isInWatchlist(coinId: String): Boolean {
        return watchlistDao.isInWatchlist(coinId)
    }
}
