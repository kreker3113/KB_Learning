package dev.kbwallet.app.chart.presentation

import androidx.compose.runtime.Stable
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.TimeRange

@Stable
data class ChartState(
    val coinName: String = "",
    val candles: List<CandleModel> = emptyList(),
    val selectedRange: TimeRange = TimeRange.ONE_DAY,
    val isLoading: Boolean = false,
    val error: org.jetbrains.compose.resources.StringResource? = null,
    val crosshairIndex: Int? = null,
    val currentPrice: Double = 0.0,
    val priceChange: Double = 0.0,
    val priceChangePercent: Double = 0.0,
    val isCandlestickMode: Boolean = false,
)
