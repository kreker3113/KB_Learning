package dev.kbwallet.app.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

data class PnLState(
    val totalTrades: Int = 0,
    val totalBuy: Int = 0,
    val totalSell: Int = 0,
    val totalInvested: String = "$0",
    val totalRealized: String = "$0",
    val realizedPnL: String = "+$0",
    val isPnLPositive: Boolean = true,
    val winRate: String = "0%",
    val bestTrade: String = "—",
    val worstTrade: String = "—",
    val avgProfitPerTrade: String = "$0",
    val activeLimitOrders: Int = 0,
    val cashBalance: String = "$0",
    val portfolioValue: String = "$0",
    val isLoading: Boolean = true,
)

class PnLViewModel(
    private val portfolioRepository: PortfolioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PnLState())
    val state: StateFlow<PnLState> = _state.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            val totalTrades = portfolioRepository.getTotalTradeCount()
            val totalBuy = portfolioRepository.getTotalBuyCount()
            val totalSell = portfolioRepository.getTotalSellCount()
            val activeOrders = portfolioRepository.getActiveLimitOrdersList()

            // Calculate P&L from transactions
            var totalInvested = 0.0
            var totalRealized = 0.0
            var bestTrade = Double.MIN_VALUE
            var worstTrade = Double.MAX_VALUE
            var profitableTrades = 0
            var totalTradeCount = 0

            portfolioRepository.getAllTransactions().collect { transactions ->
                // Simple P&L: for each SELL, calculate profit = soldAmount - (averageBuyPrice * soldUnits)
                // For now, approximate with buy/sell amounts
                transactions.forEach { tx ->
                    if (tx.type == "BUY") {
                        totalInvested += tx.amountInFiat
                    } else if (tx.type == "SELL") {
                        totalRealized += tx.amountInFiat
                        // Approximate profit
                        val avgBuy = totalInvested / (totalBuy.coerceAtLeast(1))
                        val profit = tx.amountInFiat - (tx.amountInUnit * avgBuy)
                        if (profit > bestTrade) bestTrade = profit
                        if (profit < worstTrade) worstTrade = profit
                        if (profit > 0) profitableTrades++
                        totalTradeCount++
                    }
                }
                val pnl = totalRealized - totalInvested
                val winRate = if (totalTradeCount > 0) (profitableTrades.toDouble() / totalTradeCount * 100) else 0.0
                val avgProfit = if (totalTradeCount > 0) pnl / totalTradeCount else 0.0

                _state.value = PnLState(
                    totalTrades = totalTrades,
                    totalBuy = totalBuy,
                    totalSell = totalSell,
                    totalInvested = formatFiat(totalInvested),
                    totalRealized = formatFiat(totalRealized),
                    realizedPnL = formatFiat(pnl),
                    isPnLPositive = pnl >= 0,
                    winRate = "${round(winRate).toLong()}%",
                    bestTrade = if (bestTrade != Double.MIN_VALUE) formatFiat(bestTrade) else "—",
                    worstTrade = if (worstTrade != Double.MAX_VALUE) formatFiat(worstTrade) else "—",
                    avgProfitPerTrade = formatFiat(avgProfit),
                    activeLimitOrders = activeOrders.size,
                    isLoading = false,
                )
            }
        }
    }
}
