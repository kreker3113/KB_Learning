package dev.kbwallet.app.coins.domain.api

import dev.kbwallet.app.coins.data.remote.dto.CoinDetailDto
import dev.kbwallet.app.coins.data.remote.dto.CoinMarketDto
import dev.kbwallet.app.coins.data.remote.dto.MarketChartDto
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result

interface CoinsRemoteDataSource {

    suspend fun getListOfCoins(): Result<List<CoinMarketDto>, DataError.Remote>

    suspend fun getPriceHistory(coinId: String, days: String = "1"): Result<MarketChartDto, DataError.Remote>

    suspend fun getCoinById(coinId: String): Result<CoinDetailDto, DataError.Remote>

    suspend fun getOhlc(coinId: String, days: String = "1"): Result<List<List<Double>>, DataError.Remote>
}