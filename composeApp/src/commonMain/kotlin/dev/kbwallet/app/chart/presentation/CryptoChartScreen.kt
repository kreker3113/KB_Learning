package dev.kbwallet.app.chart.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.chart.presentation.component.CandlestickChart
import dev.kbwallet.app.chart.presentation.component.ChartGrid
import dev.kbwallet.app.chart.presentation.component.LineChart
import dev.kbwallet.app.chart.presentation.component.TimeRangeSelector
import dev.kbwallet.app.chart.presentation.util.ChartFormatters
import dev.kbwallet.app.chart.presentation.util.ChartTransform
import dev.kbwallet.app.theme.DarkLossRedColor
import dev.kbwallet.app.theme.DarkProfitGreenColor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CryptoChartScreen(
    coinId: String,
    coinName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChartViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    androidx.compose.runtime.LaunchedEffect(coinId) {
        viewModel.init(coinId, coinName)
    }

    val trendColor = if (state.priceChange >= 0) DarkProfitGreenColor else DarkLossRedColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
    ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, strings.actionBack, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.coinName.ifEmpty { coinName },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (state.currentPrice > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ChartFormatters.formatPrice(state.currentPrice),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.width(10.dp))
                        val chg = state.priceChangePercent
                        val sgn = if (chg >= 0) "+" else ""
                        Text(
                            text = "$sgn${twoDec(chg)}%",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = trendColor,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.padding(vertical = 4.dp))

        // ── Time range selector + Chart mode toggle ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeRangeSelector(
                selected = state.selectedRange,
                onSelect = { viewModel.selectTimeRange(it) },
                modifier = Modifier.weight(1f),
            )

            // Candlestick / Line toggle
            FilterChip(
                selected = state.isCandlestickMode,
                onClick = { viewModel.toggleChartMode() },
                label = {
                    Text(
                        text = if (state.isCandlestickMode) strings.chartModeCandles else strings.chartModeLine,
                        fontSize = 11.sp,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = trendColor.copy(alpha = 0.2f),
                    selectedLabelColor = trendColor,
                ),
            )
        }

        Spacer(Modifier.padding(vertical = 6.dp))

        // ── Chart ──
        val transform = remember(state.candles, state.selectedRange) {
            ChartTransform(state.candles, 0f, 1f)
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(28.dp),
                    color = trendColor,
                    strokeWidth = 2.dp,
                )
            } else if (state.candles.isNotEmpty()) {
                ChartGrid(transform = transform)

                if (state.isCandlestickMode) {
                    CandlestickChart(
                        transform = transform,
                        bullColor = trendColor,
                        bearColor = DarkLossRedColor,
                        onCrosshair = { viewModel.onCrosshair(it) },
                    )
                } else {
                    LineChart(transform = transform, lineColor = trendColor)
                }

                // Price labels on right edge
                PriceLabels(
                    transform = transform,
                    currentPrice = state.currentPrice,
                    trendColor = trendColor,
                )
            } else if (state.error != null) {
                Text(
                    state.error ?: "—",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun PriceLabels(
    transform: ChartTransform,
    currentPrice: Double,
    trendColor: Color,
) {
    val vis = transform.visibleCandles
    if (vis.isEmpty()) return

    val high = vis.maxOf { it.high }
    val low = vis.minOf { it.low }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                ChartFormatters.formatPrice(high),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                ChartFormatters.formatPrice(low),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

private fun twoDec(v: Double): String {
    val r = (v * 100).toInt()
    val abs = if (r < 0) -r else r
    val sgn = if (r < 0) "-" else ""
    return "$sgn${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
}
