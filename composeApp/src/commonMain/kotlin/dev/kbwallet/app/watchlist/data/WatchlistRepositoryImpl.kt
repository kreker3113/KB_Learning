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
    override fun getWatchlistWithPrices(): Flow<dev.kbwallet.app.core.domain.Result<List<WatchlistItem>, dev.kbwallet.app.core.domain.DataError.Remote>> {
        return watchlistDao.getAllWatchlistItems().flatMapLatest { entities ->
            if (entities.isEmpty()) {
                flow { emit(dev.kbwallet.app.core.domain.Result.Success(emptyList())) }
            } else {
                flow {
                    val result = coinsRemoteDataSource.getListOfCoins()
                    val itemsResult = when (result) {
                        is dev.kbwallet.app.core.domain.Result.Success -> {
                            val items = entities.mapNotNull { entity ->
                                val coin = result.data.find { it.id == entity.coinId }
                                coin?.let {
                                    WatchlistItem(
                                        coin = dev.kbwallet.app.core.domain.coin.Coin(
                                            id = it.id,
                                            name = it.name,
                                            symbol = it.symbol.uppercase(),
                                            iconUrl = it.image,
                                        ),
                                        currentPrice = it.currentPrice,
                                        change24h = it.priceChangePercentage24h ?: 0.0,
                                        addedPrice = entity.addedPrice,
                                        addedAt = entity.addedAt,
                                    )
                                }
                            }
                            dev.kbwallet.app.core.domain.Result.Success(items)
                        }
                        is dev.kbwallet.app.core.domain.Result.Error -> {
                            dev.kbwallet.app.core.domain.Result.Error(result.error)
                        }
                    }
                    emit(itemsResult)
                }
            }
        }.catch {
            emit(dev.kbwallet.app.core.domain.Result.Error(dev.kbwallet.app.core.domain.DataError.Remote.UNKNOWN))
        }
    }

    override suspend fun isInWatchlist(coinId: String): Boolean {
        return watchlistDao.isInWatchlist(coinId)
    }
}
