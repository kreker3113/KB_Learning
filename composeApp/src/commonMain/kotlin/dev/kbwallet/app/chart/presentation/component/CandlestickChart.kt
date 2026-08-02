package dev.kbwallet.app.chart.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.isBullish
import dev.kbwallet.app.chart.presentation.util.ChartTransform

/**
 * Candlestick chart — Japanese candles with wicks.
 * Supports pan and zoom via gestures, and tap to show crosshair.
 *
 * @param transform   Data-space ↔ screen-fraction mapping
 * @param bullColor   Color for bullish candles (close >= open)
 * @param bearColor   Color for bearish candles (close < open)
 * @param onCrosshair Optional callback with the global candle index under the crosshair
 */
@Composable
fun CandlestickChart(
    transform: ChartTransform,
    modifier: Modifier = Modifier,
    bullColor: Color = Color(0xFF00FF00),
    bearColor: Color = Color(0xFFFF3B30),
    onCrosshair: ((Int?) -> Unit)? = null,
    chartHeightFraction: Float = 0.85f,
) {
    var crosshairIdx by remember { mutableIntStateOf(-1) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(transform) {
                detectTapGestures { offset ->
                    val idx = hitTest(offset.x, size.width.toFloat(), transform)
                    crosshairIdx = idx
                    onCrosshair?.invoke(idx)
                }
            }
            .pointerInput(transform) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val fraction = -(dragAmount / size.width.toFloat())
                    transform.pan(fraction)
                    crosshairIdx = -1
                    onCrosshair?.invoke(null)
                }
            }
            .pointerInput(transform) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val fraction = -(pan.x / size.width.toFloat())
                    transform.pan(fraction)
                    if (zoom != 1f) {
                        transform.zoom(zoom, 0.5f)
                    }
                    crosshairIdx = -1
                    onCrosshair?.invoke(null)
                }
            }
    ) {
        val chartHeight = size.height * chartHeightFraction
        val chartWidth = size.width
        val candles = transform.candles
        if (candles.isEmpty()) return@Canvas

        val visStart = transform.visibleStartIdx
        val visEnd = transform.visibleEndIdx
        val count = visEnd - visStart
        if (count == 0) return@Canvas

        // Calculate candle width based on viewport.
        // Bounds are expressed in dp (not raw canvas px) so bodies stay a visible
        // rectangle instead of collapsing to a sliver on high-density screens.
        val candleSpacing = chartWidth / count
        val candleBodyWidth = (candleSpacing * 0.7f).coerceIn(2.dp.toPx(), 10.dp.toPx())

        // Draw visible candles
        for (i in visStart until visEnd) {
            val c = candles[i]
            val localIdx = i - visStart
            val centerX = (localIdx.toFloat() + 0.5f) * candleSpacing

            val openY = transform.priceToFraction(c.open) * chartHeight
            val closeY = transform.priceToFraction(c.close) * chartHeight
            val highY = transform.priceToFraction(c.high) * chartHeight
            val lowY = transform.priceToFraction(c.low) * chartHeight

            val isBull = c.isBullish
            val bodyColor = if (isBull) bullColor else bearColor
            val bodyTop = if (isBull) closeY else openY
            val bodyBottom = if (isBull) openY else closeY

            // Wick (high-low line)
            drawLine(
                color = bodyColor,
                start = Offset(centerX, highY),
                end = Offset(centerX, lowY),
                strokeWidth = 1.dp.toPx(),
            )

            // Body
            val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(0f)
            if (bodyHeight < 1.dp.toPx()) {
                // Flat candle — just draw a horizontal line
                drawLine(
                    color = bodyColor,
                    start = Offset(centerX - candleBodyWidth / 2, bodyTop),
                    end = Offset(centerX + candleBodyWidth / 2, bodyTop),
                    strokeWidth = 1.5.dp.toPx(),
                )
            } else {
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(centerX - candleBodyWidth / 2, bodyTop),
                    size = Size(candleBodyWidth, bodyHeight),
                )
            }
        }

        // ── Crosshair ──
        if (crosshairIdx in visStart until visEnd) {
            val localIdx = crosshairIdx - visStart
            val cx = (localIdx.toFloat() + 0.5f) * candleSpacing
            val c = candles[crosshairIdx]

            // Vertical line
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(cx, 0f),
                end = Offset(cx, chartHeight),
                strokeWidth = 1.dp.toPx(),
            )

            // Horizontal line at close
            val closeY = transform.priceToFraction(c.close) * chartHeight
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(0f, closeY),
                end = Offset(chartWidth, closeY),
                strokeWidth = 1.dp.toPx(),
            )
        }
    }
}

/**
 * Map a pixel x-coordinate back to a global candle index, or -1 if outside.
 */
private fun hitTest(x: Float, width: Float, transform: ChartTransform): Int {
    val visStart = transform.visibleStartIdx
    val visEnd = transform.visibleEndIdx
    val count = visEnd - visStart
    if (count == 0) return -1
    val spacing = width / count
    val localIdx = (x / spacing).toInt()
    val globalIdx = localIdx + visStart
    return if (globalIdx in visStart until visEnd) globalIdx else -1
}
