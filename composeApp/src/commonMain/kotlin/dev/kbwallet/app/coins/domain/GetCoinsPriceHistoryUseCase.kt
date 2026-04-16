package dev.kbwallet.app.coins.domain

import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.coins.data.mapper.toPriceModel
import dev.kbwallet.app.coins.domain.model.PriceModel
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.map

class GetCoinPriceHistoryUseCase(
    private val client: CoinsRemoteDataSource,
) {

    suspend fun execute(coinId: String): Result<List<PriceModel>, DataError.Remote> {
        return client.getPriceHistory(coinId).map { dto ->
            dto.data.history.map { it.toPriceModel() }
        }
    }
}