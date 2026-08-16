package dev.kbwallet.app.coins.data

import dev.kbwallet.app.coins.data.remote.dto.CoinDetailDto
import dev.kbwallet.app.coins.data.remote.dto.CoinMarketDto
import dev.kbwallet.app.coins.data.remote.dto.MarketChartDto
import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result

/**
 * Hand-written fake (no mocking library in this project's test deps — see
 * FakePortfolioRepository) covering every [CoinsRemoteDataSource] method,
 * shared by CoinsListViewModel, ChartViewModel/CoinGeckoKlineDataSource, and
 * SimulatorViewModel tests.
 */
class FakeCoinsRemoteDataSource : CoinsRemoteDataSource {

    var coinsResult: Result<List<CoinMarketDto>, DataError.Remote> = Result.Success(listOf(fakeCoinMarketDto))
    var priceHistoryResult: Result<MarketChartDto, DataError.Remote> = Result.Success(MarketChartDto(prices = emptyList()))
    var coinDetailResult: Result<CoinDetailDto, DataError.Remote> = Result.Error(DataError.Remote.UNKNOWN)
    var ohlcResult: Result<List<List<Double>>, DataError.Remote> = Result.Success(fakeOhlcCandles(20))

    override suspend fun getListOfCoins(): Result<List<CoinMarketDto>, DataError.Remote> = coinsResult

    override suspend fun getPriceHistory(coinId: String, days: String): Result<MarketChartDto, DataError.Remote> =
        priceHistoryResult

    override suspend fun getCoinById(coinId: String): Result<CoinDetailDto, DataError.Remote> = coinDetailResult

    override suspend fun getOhlc(coinId: String, days: String): Result<List<List<Double>>, DataError.Remote> = ohlcResult

    companion object {
        val fakeCoinMarketDto = CoinMarketDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "https://fake.url/btc.png",
            currentPrice = 65000.0,
            priceChangePercentage24h = 2.5,
            marketCapRank = 1,
        )

        /**
         * [count] flat OHLC candles (CoinGecko's `[timestamp, open, high, low, close]`
         * format, one per hour) — enough by default to clear SimulatorViewModel's
         * "at least 10 candles" guard.
         */
        fun fakeOhlcCandles(count: Int, price: Double = 100.0): List<List<Double>> =
            (0 until count).map { i ->
                listOf((i * 3_600_000L).toDouble(), price, price + 1, price - 1, price)
            }
    }
}
