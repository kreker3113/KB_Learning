package dev.kbwallet.app.simulator.presentation

import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.TimeRange
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.core.i18n.AppStrings
import dev.kbwallet.app.simulator.domain.ClosedTrade
import dev.kbwallet.app.simulator.domain.SimPosition
import dev.kbwallet.app.simulator.domain.SimulatorMetrics

data class SimulatorState(
    // ── Coin selection ──
    val selectedCoin: Coin? = null,
    val coinSearchQuery: String = "",
    val availableCoins: List<Coin> = emptyList(),

    // ── Simulation data ──
    val candles: List<CandleModel> = emptyList(),
    val currentCandleIndex: Int = 0,
    val isPlaying: Boolean = false,
    val playSpeed: PlaySpeed = PlaySpeed.NORMAL,
    val timeRange: TimeRange = TimeRange.ONE_DAY,

    // ── User portfolio (simulator balance) ──
    val initialBalance: Double = 10000.0,
    val cashBalance: Double = 10000.0,
    val equity: Double = 10000.0,          // cash + unrealized P&L
    val balanceHistory: List<Double> = emptyList(), // equity at each candle

    // ── Positions ──
    val positions: List<SimPosition> = emptyList(),

    // ── Order Form ──
    val orderAmount: String = "",
    val orderSide: OrderSideInput = OrderSideInput.LONG,
    val orderStopLoss: String = "",
    val orderTakeProfit: String = "",
    val orderLeverage: String = "1",

    // ── Closed trades ──
    val closedTrades: List<ClosedTrade> = emptyList(),

    // ── Metrics ──
    val metrics: SimulatorMetrics = SimulatorMetrics(),

    // ── UI state ──
    val isLoading: Boolean = true,
    val error: SimulatorErrorType? = null,
    val activeHint: String? = null,
    val showHeatmap: Boolean = false,
)

/**
 * Kept as an enum (previously a hardcoded, English-only literal `String?`)
 * so the message can be localized like everything else — the ViewModel layer
 * has no [AppStrings]/composition access, so it can only report *which*
 * error happened, not the final display text (see [label]).
 */
enum class SimulatorErrorType {
    FAILED_TO_LOAD_COINS,
    NOT_ENOUGH_DATA,
    FAILED_TO_LOAD_DATA,
}

fun SimulatorErrorType.label(strings: AppStrings): String = when (this) {
    SimulatorErrorType.FAILED_TO_LOAD_COINS -> strings.simulatorErrorFailedToLoadCoins
    SimulatorErrorType.NOT_ENOUGH_DATA -> strings.simulatorErrorNotEnoughData
    SimulatorErrorType.FAILED_TO_LOAD_DATA -> strings.simulatorErrorFailedToLoadData
}

enum class PlaySpeed(val label: String, val delayMs: Long) {
    SLOW("0.5x", 2000L),
    NORMAL("1x", 1000L),
    FAST("2x", 400L),
    MAX("10x", 80L),
}

enum class OrderSideInput(val label: String) {
    LONG("Long"),
    SHORT("Short"),
}
