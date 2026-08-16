package dev.kbwallet.app.chart.data

import dev.kbwallet.app.chart.domain.api.KlineDataSource
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.TimeRange
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result

/** Hand-written fake — see FakeCoinsRemoteDataSource for the project's testing convention. */
class FakeKlineDataSource : KlineDataSource {

    var result: Result<List<CandleModel>, DataError.Remote> = Result.Success(fakeCandles(20))

    override suspend fun fetchKlines(symbol: String, interval: TimeRange): Result<List<CandleModel>, DataError.Remote> =
        result

    companion object {
        fun fakeCandles(count: Int, startPrice: Double = 100.0): List<CandleModel> =
            (0 until count).map { i ->
                val price = startPrice + i
                CandleModel(
                    openTime = i * 3_600_000L,
                    open = price,
                    high = price + 1,
                    low = price - 1,
                    close = price,
                    volume = 10.0,
                )
            }
    }
}
