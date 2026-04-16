package dev.kbwallet.app.coins.domain.api

import dev.kbwallet.app.coins.data.remote.dto.CoinDetailsResponseDto
import dev.kbwallet.app.coins.data.remote.dto.CoinPriceHistoryResponseDto
import dev.kbwallet.app.coins.data.remote.dto.CoinsResponseDto
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result

interface CoinsRemoteDataSource {

    suspend fun getListOfCoins(): Result<CoinsResponseDto, DataError.Remote>

    suspend fun getPriceHistory(coinId: String): Result<CoinPriceHistoryResponseDto, DataError.Remote>

    suspend fun getCoinById(coinId: String): Result<CoinDetailsResponseDto, DataError.Remote>
}