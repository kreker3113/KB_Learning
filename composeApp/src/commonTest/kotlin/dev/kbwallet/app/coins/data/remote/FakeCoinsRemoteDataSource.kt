package dev.kbwallet.app.coins.data.remote

import dev.kbwallet.app.coins.data.remote.dto.CoinDetailDto
import dev.kbwallet.app.coins.data.remote.dto.CoinMarketDto
import dev.kbwallet.app.coins.data.remote.dto.MarketChartDto
import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result

import dev.kbwallet.app.coins.data.remote.dto.CoinImageDto
import dev.kbwallet.app.coins.data.remote.dto.MarketDataDto

class FakeCoinsRemoteDataSource : CoinsRemoteDataSource {

    var simulateError: Boolean = false
    var coinsResult: List<CoinMarketDto> = listOf(
        CoinMarketDto("1", "BTC", "Bitcoin", "icon", 50000.0, 5.0, 1)
    )

    override suspend fun getListOfCoins(): Result<List<CoinMarketDto>, DataError.Remote> {
        if (simulateError) {
            return Result.Error(DataError.Remote.SERVER)
        }
        return Result.Success(coinsResult)
    }

    override suspend fun getPriceHistory(
        coinId: String,
        days: String
    ): Result<MarketChartDto, DataError.Remote> {
        if (simulateError) {
            return Result.Error(DataError.Remote.SERVER)
        }
        return Result.Success(MarketChartDto(emptyList()))
    }

    override suspend fun getCoinById(coinId: String): Result<CoinDetailDto, DataError.Remote> {
        if (simulateError) {
            return Result.Error(DataError.Remote.SERVER)
        }
        return Result.Success(CoinDetailDto("1", "BTC", "Bitcoin", CoinImageDto("", "", ""), MarketDataDto()))
    }

    override suspend fun getOhlc(
        coinId: String,
        days: String
    ): Result<List<List<Double>>, DataError.Remote> {
        if (simulateError) {
            return Result.Error(DataError.Remote.SERVER)
        }
        val dummyCandles = List(10) { index -> 
            listOf(index.toDouble() * 1000, 100.0, 110.0, 90.0, 105.0)
        }
        return Result.Success(dummyCandles)
    }
}
