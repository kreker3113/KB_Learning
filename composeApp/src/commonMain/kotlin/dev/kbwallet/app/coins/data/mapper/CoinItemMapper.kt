package dev.kbwallet.app.coins.data.mapper

import dev.kbwallet.app.coins.data.remote.dto.CoinDetailDto
import dev.kbwallet.app.coins.data.remote.dto.CoinMarketDto
import dev.kbwallet.app.coins.data.remote.dto.MarketChartDto
import dev.kbwallet.app.coins.domain.model.CoinModel
import dev.kbwallet.app.coins.domain.model.PriceModel
import dev.kbwallet.app.core.domain.coin.Coin

fun CoinMarketDto.toCoinModel() = CoinModel(
    coin = Coin(
        id = id,
        name = name,
        symbol = symbol.uppercase(),
        iconUrl = image,
    ),
    price = currentPrice,
    change = priceChangePercentage24h ?: 0.0,
)

fun CoinDetailDto.toCoinModel() = CoinModel(
    coin = Coin(
        id = id,
        name = name,
        symbol = symbol.uppercase(),
        iconUrl = image.large,
    ),
    price = marketData?.currentPrice?.get("usd") ?: 0.0,
    change = marketData?.priceChangePercentage24h ?: 0.0,
)

/**
 * CoinGecko market_chart returns prices as [[timestamp_ms, price], ...]
 * We convert timestamps from millis to seconds for consistency with domain model.
 */
fun MarketChartDto.toPriceModels(): List<PriceModel> =
    prices.mapNotNull { entry ->
        if (entry.size >= 2) {
            PriceModel(
                price = entry[1],
                timestamp = (entry[0] / 1000).toLong(), // ms → seconds
            )
        } else null
    }