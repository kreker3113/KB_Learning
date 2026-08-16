package dev.kbwallet.app.trade.presentation.common

import org.jetbrains.compose.resources.StringResource

data class TradeState(
    val isLoading: Boolean = false,
    val error: StringResource? = null,
    val availableAmount: String = "",
    val availableBalance: Double = 0.0,
    val amount: String = "",
    val coin: UiTradeCoinItem? = null,
    val isAmountInUnits: Boolean = false,
    val fiatEquivalent: String = "",
)
