package dev.kbwallet.app.dashboard.presentation

import org.jetbrains.compose.resources.StringResource

data class DashboardState(
    val isLoading: Boolean = false,
    val error: StringResource? = null,
    val portfolioValue: String = "$0",
    val coinCount: Int = 0,
    val recentPerformance: String = "+0%",
    val isPerformancePositive: Boolean = true,
    val topCoins: List<DashboardCoinItem> = emptyList(),
    val portfolioSummaryCoins: List<DashboardCoinItem> = emptyList(),
)

data class DashboardCoinItem(
    val id: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val formattedPrice: String,
    val formattedChange: String,
    val isPositive: Boolean,
)
