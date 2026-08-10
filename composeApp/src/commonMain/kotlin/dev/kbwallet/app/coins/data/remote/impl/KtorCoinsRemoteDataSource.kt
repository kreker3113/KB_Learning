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

private const val BASE_URL = "https://api.coingecko.com/api/v3"

class KtorCoinsRemoteDataSource(
    private val httpClient: HttpClient
) : CoinsRemoteDataSource {

    override suspend fun getListOfCoins(): Result<List<CoinMarketDto>, DataError.Remote> {
        return safeCall {
            httpClient.get("$BASE_URL/coins/markets") {
                url.parameters.append("vs_currency", "usd")
                url.parameters.append("order", "market_cap_desc")
                url.parameters.append("per_page", "50")
                url.parameters.append("page", "1")
                url.parameters.append("sparkline", "false")
            }
        }
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