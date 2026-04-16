package dev.kbwallet.app.coins.data.mapper

import dev.kbwallet.app.coins.data.remote.dto.CoinItemDto
import dev.kbwallet.app.coins.data.remote.dto.CoinPriceDto
import dev.kbwallet.app.coins.domain.model.CoinModel
import dev.kbwallet.app.coins.domain.model.PriceModel
import dev.kbwallet.app.core.domain.coin.Coin

fun CoinItemDto.toCoinModel() = CoinModel(
    coin = Coin(
        id = uuid,
        name = name,
        symbol = symbol,
        iconUrl = iconUrl,
    ),
    price = price,
    change = change,
)
fun CoinPriceDto.toPriceModel() = PriceModel(
    price = price ?: 0.0,
    timestamp = timestamp,
)