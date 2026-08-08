package dev.kbwallet.app.coins.data.remote.impl

import dev.kbwallet.app.coins.data.remote.dto.CoinDetailsResponseDto
import dev.kbwallet.app.coins.data.remote.dto.CoinPriceHistoryResponseDto
import dev.kbwallet.app.coins.data.remote.dto.CoinsResponseDto
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

private const val BASE_URL = "https://api.coinranking.com/v2"

class KtorCoinsRemoteDataSource (
    private val httpClient: HttpClient
) : CoinsRemoteDataSource {

    // CoinRanking's free tier caps requests at 5/second. Dashboard, the coin
    // list, the simulator's coin picker, and portfolio valuation all call
    // getListOfCoins() independently — visiting a couple of them within the
    // same second (completely normal navigation, not just aggressive testing)
    // was enough to trip that ceiling. The API itself advertises
    // `cache-control: max-age=60` on this endpoint, so a short in-memory
    // cache at the same TTL is safe and cuts most of that duplicate traffic.
    private val listCacheMutex = Mutex()
    private var cachedList: Result<CoinsResponseDto, DataError.Remote>? = null
    private var cachedAt: Instant = Instant.DISTANT_PAST
    private val listCacheTtl = 60.seconds

    override suspend fun getListOfCoins(): Result<CoinsResponseDto, DataError.Remote> {
        listCacheMutex.withLock {
            val cached = cachedList
            if (cached != null && Clock.System.now() - cachedAt < listCacheTtl) {
                return cached
            }
        }

        val result = safeCall<CoinsResponseDto> {
            httpClient.get("$BASE_URL/coins")
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

    override suspend fun getPriceHistory(coinId: String, timePeriod: String): Result<CoinPriceHistoryResponseDto, DataError.Remote> {
        return safeCall {
            httpClient.get("$BASE_URL/coin/$coinId/history") {
                url.parameters.append("timePeriod", timePeriod)
            }
        }
    }

    override suspend fun getCoinById(coinId: String): Result<CoinDetailsResponseDto, DataError.Remote> {
        return safeCall {
            httpClient.get("$BASE_URL/coin/$coinId")
        }
    }
}
