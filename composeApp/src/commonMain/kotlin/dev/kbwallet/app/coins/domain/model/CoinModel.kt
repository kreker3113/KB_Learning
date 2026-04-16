package dev.kbwallet.app.coins.domain.model

import dev.kbwallet.app.core.domain.coin.Coin

data class CoinModel (
    val coin: Coin,
    val price: Double,
    val change: Double,
)