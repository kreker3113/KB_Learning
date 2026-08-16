package dev.kbwallet.app.simulator.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.kbwallet.app.chart.presentation.component.CandlestickChart
import dev.kbwallet.app.chart.presentation.component.ChartGrid
import dev.kbwallet.app.chart.presentation.component.LineChart
import dev.kbwallet.app.chart.presentation.component.SMAOverlay
import dev.kbwallet.app.chart.presentation.util.ChartTransform
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.simulator.domain.*
import dev.kbwallet.app.theme.DarkLossRedColor
import dev.kbwallet.app.theme.DarkProfitGreenColor
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import dev.kbwallet.app.theme.component.ErrorRetryCard
import dev.kbwallet.app.theme.component.StatCard
import dev.kbwallet.app.theme.component.StatCardSize
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.math.round

/** Format a Double to N decimal places (KMP-safe, no String.format). */
private fun fmtDec(value: Double, decimals: Int): String {
    if (decimals <= 0) return round(value).toLong().toString()
    val factor = listOf(1.0, 10.0, 100.0, 1000.0).getOrElse(decimals) { 1.0 }
    val rounded = round(value * factor) / factor
    val parts = rounded.toString().split('.')
    val intPart = parts[0]
    val fracPart = (parts.getOrElse(1) { "0" }).take(decimals).padEnd(decimals, '0')
    return "$intPart.$fracPart"
}

private val SubtextGray = Color(0xFFAAAAAA)

@Composable
fun SimulatorScreen(
    onBack: () -> Unit,
    viewModel: SimulatorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    LaunchedEffect(Unit) {
        viewModel.loadCoins()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.actionBack, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = strings.simulatorTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            if (state.activeHint != null) {
                IconButton(onClick = { viewModel.nextHint() }) {
                    Icon(Icons.Default.Info, strings.simulatorHintContentDesc, tint = Color(0xFFFFA500))
                }
            }
        }

        if (state.isLoading && state.availableCoins.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        if (state.error != null && state.availableCoins.isEmpty() && state.selectedCoin == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                ErrorRetryCard(
                    message = state.error!!.label(strings),
                    onRetry = { viewModel.loadCoins() },
                    modifier = Modifier.padding(24.dp),
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Coin Selection ──
            if (state.selectedCoin == null) {
                item { Text(strings.simulatorSelectCoinPrompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(state.availableCoins) { coin ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.selectCoin(coin) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(coin.iconUrl, null, contentScale = ContentScale.Fit, modifier = Modifier.size(20.dp).clip(CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("${coin.name} (${coin.symbol.uppercase()})", fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (state.selectedCoin != null && state.candles.isEmpty() && state.error != null && !state.isLoading) {
                item {
                    ErrorRetryCard(
                        message = state.error!!.label(strings),
                        onRetry = { viewModel.selectCoin(state.selectedCoin!!) },
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }

            if (state.selectedCoin != null && state.candles.isNotEmpty()) {
                val coin = state.selectedCoin!!
                val candle = state.candles.getOrNull(state.currentCandleIndex)

                // ── Coin info + Price ──
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(coin.iconUrl, null, contentScale = ContentScale.Fit, modifier = Modifier.size(28.dp).clip(CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("${coin.name} (${coin.symbol.uppercase()})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        if (candle != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$${candle.close}",
                                    fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    strings.simulatorCandleCounter(state.currentCandleIndex + 1, state.candles.size),
                                    fontSize = 10.sp, color = SubtextGray,
                                )
                            }
                        }
                    }
                }

                // ── Playback Controls ──
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { viewModel.stepBackward() }) {
                                Icon(Icons.Default.SkipPrevious, strings.simulatorPrevContentDesc)
                            }
                            IconButton(onClick = { viewModel.togglePlay() }) {
                                Icon(
                                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    if (state.isPlaying) strings.simulatorPauseContentDesc else strings.simulatorPlayContentDesc,
                                )
                            }
                            IconButton(onClick = { viewModel.stepForward() }) {
                                Icon(Icons.Default.SkipNext, strings.simulatorNextContentDesc)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PlaySpeed.entries.forEach { speed ->
                                FilterChip(
                                    selected = state.playSpeed == speed,
                                    onClick = { viewModel.setPlaySpeed(speed) },
                                    label = { Text(speed.label, fontSize = 10.sp) },
                                )
                            }
                        }
                    }
                }

                // ── Chart (Balance + Price) ──
                item {
                    val transform = remember(state.candles, state.currentCandleIndex) {
                        val visibleRange = if (state.candles.size > 0) {
                            val window = 60.coerceAtMost(state.candles.size)
                            val start = (state.currentCandleIndex - window / 2).coerceIn(0, state.candles.size - window)
                            val end = start + window
                            start.toFloat() / state.candles.size to end.toFloat() / state.candles.size
                        } else 0f to 1f
                        ChartTransform(state.candles, visibleRange.first, visibleRange.second)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        ChartGrid(transform = transform)
                        CandlestickChart(
                            transform = transform,
                            bullColor = DarkProfitGreenColor,
                            bearColor = DarkLossRedColor,
                            chartHeightFraction = 0.7f,
                        )

                    }
                }

                // ── Balance / Equity ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StatCard(strings.simulatorStatBalance, formatFiat(state.cashBalance), Modifier.weight(1f), size = StatCardSize.Compact)
                        StatCard(strings.simulatorStatEquity, formatFiat(state.equity), Modifier.weight(1f), DarkProfitGreenColor, size = StatCardSize.Compact)
                        val pnl = state.equity - state.initialBalance
                        StatCard(strings.simulatorStatPnl, formatFiat(pnl), Modifier.weight(1f), if (pnl >= 0) DarkProfitGreenColor else DarkLossRedColor, size = StatCardSize.Compact)
                    }
                }

                // ── Order Form ──
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(strings.simulatorNewPositionTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            // Side selector
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OrderSideInput.entries.forEach { side ->
                                    FilterChip(
                                        selected = state.orderSide == side,
                                        onClick = { viewModel.onOrderSideChanged(side) },
                                        label = { Text(side.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = if (side == OrderSideInput.LONG)
                                                DarkProfitGreenColor.copy(alpha = 0.2f)
                                            else DarkLossRedColor.copy(alpha = 0.2f),
                                            selectedLabelColor = if (side == OrderSideInput.LONG)
                                                DarkProfitGreenColor else DarkLossRedColor,
                                        ),
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Amount + Leverage
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OrderField(
                                    label = strings.simulatorAmountLabel,
                                    value = state.orderAmount,
                                    onValueChange = { viewModel.onOrderAmountChanged(it) },
                                    modifier = Modifier.weight(1f),
                                )
                                OrderField(
                                    label = strings.simulatorLeverageLabel,
                                    value = state.orderLeverage,
                                    onValueChange = { viewModel.onOrderLeverageChanged(it) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // Liquidation price preview — leverage means a much smaller
                            // adverse move can wipe the margin than beginners tend to expect.
                            val previewLeverage = state.orderLeverage.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0
                            val previewEntry = state.candles.getOrNull(state.currentCandleIndex)?.close
                            if (previewLeverage > 1.0 && previewEntry != null) {
                                val move = previewEntry / previewLeverage
                                val liqPreview = if (state.orderSide == OrderSideInput.LONG)
                                    (previewEntry - move).coerceAtLeast(0.0) else previewEntry + move
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                        .background(DarkLossRedColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = DarkLossRedColor, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = strings.simulatorLiquidatesAt(formatFiat(liqPreview), fmtDec(100.0 / previewLeverage, 1)),
                                        color = DarkLossRedColor,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            // Stop-Loss + Take-Profit
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OrderField(
                                    label = strings.simulatorStopLossLabel,
                                    value = state.orderStopLoss,
                                    onValueChange = { viewModel.onOrderSLChanged(it) },
                                    accentColor = DarkLossRedColor,
                                    modifier = Modifier.weight(1f),
                                )
                                OrderField(
                                    label = strings.simulatorTakeProfitLabel,
                                    value = state.orderTakeProfit,
                                    onValueChange = { viewModel.onOrderTPChanged(it) },
                                    accentColor = DarkProfitGreenColor,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.openPosition() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.orderSide == OrderSideInput.LONG)
                                        DarkProfitGreenColor else DarkLossRedColor,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    if (state.orderSide == OrderSideInput.LONG) strings.simulatorLongAtMarket else strings.simulatorShortAtMarket,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }

                // ── Open Positions ──
                if (state.positions.isNotEmpty()) {
                    item {
                        Text(strings.simulatorOpenPositions(state.positions.size), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(state.positions, key = { it.id }) { pos ->
                        PositionCard(pos = pos, onClose = { viewModel.closePosition(pos.id) })
                    }
                }

                // ── Hint Banner ──
                if (state.activeHint != null) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(Color(0xFFFFA500).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.nextHint() }
                                .padding(12.dp)
                        ) {
                            Text(state.activeHint!!, fontSize = 12.sp, color = Color(0xFFFFA500))
                        }
                    }
                }

                // ── Metrics ──
                if (state.closedTrades.isNotEmpty()) {
                    val m = state.metrics
                    item { Text(strings.simulatorMetricsTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                            StatCard(strings.simulatorMetricWinRate, "${fmtDec(m.winRate * 100, 0)}%", Modifier.weight(1f), size = StatCardSize.Compact, monospaceValue = true)
                            StatCard(strings.simulatorMetricProfitFactor, fmtDec(m.profitFactor, 2), Modifier.weight(1f), size = StatCardSize.Compact, monospaceValue = true)
                            StatCard(strings.simulatorMetricTrades, "${m.totalTrades}", Modifier.weight(1f), size = StatCardSize.Compact, monospaceValue = true)
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                            StatCard(strings.simulatorMetricMaxDD, "$${fmtDec(m.maxDrawdown, 0)}", Modifier.weight(1f), DarkLossRedColor, size = StatCardSize.Compact, monospaceValue = true)
                            StatCard(strings.simulatorMetricBest, "$${fmtDec(m.bestTrade, 0)}", Modifier.weight(1f), DarkProfitGreenColor, size = StatCardSize.Compact, monospaceValue = true)
                            StatCard(strings.simulatorMetricSharpe, fmtDec(m.sharpeRatio, 2), Modifier.weight(1f), size = StatCardSize.Compact, monospaceValue = true)
                        }
                    }
                }

                // ── Closed Trades ──
                if (state.closedTrades.isNotEmpty()) {
                    item { Text(strings.simulatorTradeHistoryTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                    items(state.closedTrades.reversed()) { trade ->
                        ClosedTradeCard(trade)
                    }
                }

                // ── Bottom spacer for nav bar ──
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PositionCard(pos: SimPosition, onClose: () -> Unit) {
    val strings = appStrings()
    val pnlColor = if (pos.pnl >= 0) DarkProfitGreenColor else DarkLossRedColor
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${pos.side.name} ${pos.coinSymbol.uppercase()}",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text("$${pos.amountInFiat.toInt()}", fontSize = 13.sp, color = SubtextGray)
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Text(strings.simulatorEntryLabel(formatFiat(pos.entryPrice)), fontSize = 12.sp, color = SubtextGray)
                Spacer(Modifier.width(12.dp))
                Text(strings.simulatorNowLabel(formatFiat(pos.currentPrice)), fontSize = 12.sp, color = SubtextGray)
                Spacer(Modifier.weight(1f))
                Text(strings.simulatorPnlLine(fmtDec(pos.pnl, 0), fmtDec(pos.pnlPercent, 1)),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = pnlColor)
            }
            if (pos.stopLoss != null || pos.takeProfit != null) {
                Spacer(Modifier.height(2.dp))
                Row {
                    if (pos.stopLoss != null) Text(strings.simulatorSlLabel(formatFiat(pos.stopLoss)), fontSize = 11.sp, color = DarkLossRedColor)
                    Spacer(Modifier.width(12.dp))
                    if (pos.takeProfit != null) Text(strings.simulatorTpLabel(formatFiat(pos.takeProfit)), fontSize = 11.sp, color = DarkProfitGreenColor)
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = pnlColor.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(strings.simulatorCloseButton, fontSize = 12.sp, color = pnlColor)
            }
        }
    }
}

@Composable
private fun ClosedTradeCard(trade: ClosedTrade) {
    val strings = appStrings()
    val pnlColor = if (trade.pnl >= 0) DarkProfitGreenColor else DarkLossRedColor
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (trade.side == PositionSide.LONG) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                null, tint = pnlColor, modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${trade.side.name} ${trade.coinName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(strings.simulatorEntryExitLabel(formatFiat(trade.entryPrice), formatFiat(trade.exitPrice)), fontSize = 11.sp, color = SubtextGray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${fmtDec(trade.pnl, 0)} (${fmtDec(trade.pnlPercent, 1)}%)",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = pnlColor)
                Text(trade.exitReason.name, fontSize = 10.sp, color = SubtextGray)
            }
        }
    }
}

@Composable
private fun OrderField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(fontSize = 14.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.height(56.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor ?: MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = accentColor ?: MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = SubtextGray,
            cursorColor = accentColor ?: MaterialTheme.colorScheme.primary,
        ),
    )
}

