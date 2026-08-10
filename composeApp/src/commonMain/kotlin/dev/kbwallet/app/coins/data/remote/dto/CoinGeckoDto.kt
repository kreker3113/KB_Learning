package dev.kbwallet.app.coins.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response item for GET /coins/markets
 * Returns a flat list: List<CoinMarketDto>
 */
@Serializable
data class CoinMarketDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerialName("current_price")
    val currentPrice: Double,
    @SerialName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
    @SerialName("market_cap_rank")
    val marketCapRank: Int? = null,
)

/**
 * Response for GET /coins/{id}
 */
@Serializable
data class CoinDetailDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: CoinImageDto,
    @SerialName("market_data")
    val marketData: MarketDataDto? = null,
)

@Serializable
data class CoinImageDto(
    val large: String,
    val small: String,
    val thumb: String,
)

@Serializable
data class MarketDataDto(
    @SerialName("current_price")
    val currentPrice: Map<String, Double> = emptyMap(),
    @SerialName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
)

/**
 * Response for GET /coins/{id}/market_chart
 * prices = [[timestamp_ms, price], ...]
 */
@Serializable
data class MarketChartDto(
    val prices: List<List<Double>>,
)
