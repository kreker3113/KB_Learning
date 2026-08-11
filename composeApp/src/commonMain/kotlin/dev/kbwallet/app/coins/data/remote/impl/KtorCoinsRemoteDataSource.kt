package dev.kbwallet.app.coins.data.remote.impl

import dev.kbwallet.app.coins.data.remote.dto.CoinDetailDto
import dev.kbwallet.app.coins.data.remote.dto.CoinMarketDto
import dev.kbwallet.app.coins.data.remote.dto.MarketChartDto
import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

private const val BASE_URL = "https://api.coingecko.com/api/v3"

class KtorCoinsRemoteDataSource(
    private val httpClient: HttpClient
) : CoinsRemoteDataSource {

    // CoinGecko's free tier also rate-limits aggressively. Dashboard, the coin
    // list, the simulator's coin picker, and portfolio valuation all call
    // getListOfCoins() independently — visiting a couple of them within the
    // same second (completely normal navigation, not just aggressive testing)
    // was enough to trip that ceiling. A short in-memory cache cuts most of
    // that duplicate traffic.
    private val listCacheMutex = Mutex()
    private var cachedList: Result<List<CoinMarketDto>, DataError.Remote>? = null
    private var cachedAt: Instant = Instant.DISTANT_PAST
    private val listCacheTtl = 60.seconds

    override suspend fun getListOfCoins(): Result<List<CoinMarketDto>, DataError.Remote> {
        listCacheMutex.withLock {
            val cached = cachedList
            if (cached != null && Clock.System.now() - cachedAt < listCacheTtl) {
                return cached
            }
        }

        val result = safeCall<List<CoinMarketDto>> {
            httpClient.get("$BASE_URL/coins/markets") {
                url.parameters.append("vs_currency", "usd")
                url.parameters.append("order", "market_cap_desc")
                url.parameters.append("per_page", "50")
                url.parameters.append("page", "1")
                url.parameters.append("sparkline", "false")
            }
        }

        // Only cache real data — an error/rate-limit response should be
        // retried on the next call, not remembered for a full minute.
        if (result is Result.Success) {
            listCacheMutex.withLock {
                cachedList = result
                cachedAt = Clock.System.now()
            }
        }
        return result
    }

    override suspend fun getPriceHistory(
        coinId: String,
        days: String,
    ): Result<MarketChartDto, DataError.Remote> {
        return safeCall {
            httpClient.get("$BASE_URL/coins/$coinId/market_chart") {
                url.parameters.append("vs_currency", "usd")
                url.parameters.append("days", days)
            }
        }
    }

    override suspend fun getCoinById(coinId: String): Result<CoinDetailDto, DataError.Remote> {
        return safeCall {
            httpClient.get("$BASE_URL/coins/$coinId") {
                url.parameters.append("localization", "false")
                url.parameters.append("tickers", "false")
                url.parameters.append("community_data", "false")
                url.parameters.append("developer_data", "false")
            }
        }
    }

    override suspend fun getOhlc(
        coinId: String,
        days: String,
    ): Result<List<List<Double>>, DataError.Remote> {
        return safeCall {
            httpClient.get("$BASE_URL/coins/$coinId/ohlc") {
                url.parameters.append("vs_currency", "usd")
                url.parameters.append("days", days)
            }
        }
    }
}
