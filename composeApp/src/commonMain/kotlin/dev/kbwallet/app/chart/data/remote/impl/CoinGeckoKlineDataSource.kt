package dev.kbwallet.app.chart.data.remote.impl

import dev.kbwallet.app.chart.domain.api.KlineDataSource
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.TimeRange
import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result

/**
 * CoinGecko-backed OHLC data source.
 * Uses the /coins/{id}/ohlc endpoint which returns real OHLC candle data.
 * Response format: [[timestamp, open, high, low, close], ...]
 */
class CoinGeckoKlineDataSource(
    private val coinsApi: CoinsRemoteDataSource,
) : KlineDataSource {

    override suspend fun fetchKlines(
        symbol: String,   // CoinGecko coin ID (e.g. "bitcoin")
        interval: TimeRange,
    ): Result<List<CandleModel>, DataError.Remote> {
        return when (val result = coinsApi.getOhlc(symbol, interval.coinGeckoDays)) {
            is Result.Success -> {
                val candles = result.data.mapNotNull { entry ->
                    if (entry.size >= 5) {
                        CandleModel(
                            openTime = entry[0].toLong(),  // already in millis
                            open = entry[1],
                            high = entry[2],
                            low = entry[3],
                            close = entry[4],
                            volume = 0.0,   // CoinGecko OHLC doesn't include volume
                        )
                    } else null
                }.sortedBy { it.openTime }
                Result.Success(candles)
            }
            is Result.Error -> result
        }
    }
}
