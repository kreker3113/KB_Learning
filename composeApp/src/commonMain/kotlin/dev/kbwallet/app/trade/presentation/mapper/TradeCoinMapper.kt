package dev.kbwallet.app.trade.presentation.mapper

import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.trade.presentation.common.UiTradeCoinItem

fun UiTradeCoinItem.toCoin() = Coin(
    id = id,
    name = name,
    symbol = symbol,
    iconUrl = iconUrl,
    )