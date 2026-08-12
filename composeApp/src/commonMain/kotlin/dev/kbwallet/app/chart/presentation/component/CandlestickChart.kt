package dev.kbwallet.app.chart.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.isBullish
import dev.kbwallet.app.chart.presentation.util.ChartTransform
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    val textMeasurer = rememberTextMeasurer()
    var crosshairIdx by remember { mutableIntStateOf(-1) }
    // MaterialTheme is a @Composable reader — has to be resolved here, in the
    // composition, and captured; the Canvas content lambda below runs as a
    // DrawScope during the draw phase and can't call it directly.
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(transform) {
                detectTapGestures { offset ->
                    val rightMargin = 55.dp.toPx()
                    val chartWidth = size.width - rightMargin
                    if (offset.x <= chartWidth) {
                        val idx = hitTest(offset.x, chartWidth, transform)
                        crosshairIdx = idx
                        onCrosshair?.invoke(idx)
                    } else {
                        crosshairIdx = -1
                        onCrosshair?.invoke(null)
                    }
                }
            }
            .pointerInput(transform) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val rightMargin = 55.dp.toPx()
                    val chartWidth = size.width - rightMargin
                    val fraction = -(pan.x / chartWidth)
                    transform.pan(fraction)
                    if (zoom != 1f) {
                        transform.zoom(zoom, 0.5f)
                    }
                    crosshairIdx = -1
                    onCrosshair?.invoke(null)
                }
            }
    ) {
        val rightMargin = 55.dp.toPx()
        val chartHeight = size.height * chartHeightFraction
        val chartWidth = size.width - rightMargin
        
        val candles = transform.candles
        if (candles.isEmpty()) return@Canvas

        val visStart = transform.visibleStartIdx
        val visEnd = transform.visibleEndIdx
        val count = visEnd - visStart
        if (count == 0) return@Canvas

        // Calculate candle width based on viewport.
        val candleSpacing = chartWidth / count
        val candleBodyWidth = (candleSpacing * 0.7f).coerceAtLeast(2.dp.toPx())

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

            // Volume bar
            val volFraction = transform.volumeToFraction(c.volume)
            if (volFraction > 0f) {
                val volHeight = (volFraction * chartHeight * 0.25f).coerceAtLeast(1f) // max 25% of chart height
                drawRect(
                    color = bodyColor.copy(alpha = 0.3f),
                    topLeft = Offset(centerX - candleBodyWidth / 2, chartHeight - volHeight),
                    size = Size(candleBodyWidth, volHeight)
                )
            }

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

        // ── Y-Axis (Prices) ──
        val priceRange = transform.priceRange
        val minPrice = priceRange.start
        val maxPrice = priceRange.endInclusive
        val steps = 5
        val priceStep = (maxPrice - minPrice) / steps
        val axisTextStyle = TextStyle(color = axisColor, fontSize = 10.sp, fontWeight = FontWeight.Normal)

        for (i in 0..steps) {
            val p = minPrice + priceStep * i
            val y = transform.priceToFraction(p) * chartHeight
            val label = dev.kbwallet.app.chart.presentation.util.ChartFormatters.formatPrice(p)
            
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                style = axisTextStyle,
                topLeft = Offset(chartWidth + 4.dp.toPx(), y - 6.dp.toPx())
            )
        }

        // ── X-Axis (Time) ──
        val timeSteps = 4
        val timeStepIdx = (count / timeSteps).coerceAtLeast(1)
        for (i in visStart..visEnd step timeStepIdx) {
            if (i >= candles.size) break
            val c = candles[i]
            val localIdx = i - visStart
            val cx = (localIdx.toFloat() + 0.5f) * candleSpacing
            val label = dev.kbwallet.app.chart.presentation.util.ChartFormatters.formatTimeShort(c.openTime)
            
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                style = axisTextStyle,
                topLeft = Offset(cx - 15.dp.toPx(), chartHeight + 4.dp.toPx())
            )
        }

        // ── Crosshair ──
        if (crosshairIdx in visStart until visEnd) {
            val localIdx = crosshairIdx - visStart
            val cx = (localIdx.toFloat() + 0.5f) * candleSpacing
            val c = candles[crosshairIdx]

            // Vertical line
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(cx, 0f),
                end = Offset(cx, chartHeight),
                strokeWidth = 1.dp.toPx(),
            )

            // Horizontal line at close
            val closeY = transform.priceToFraction(c.close) * chartHeight
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, closeY),
                end = Offset(chartWidth, closeY),
                strokeWidth = 1.dp.toPx(),
            )

            // Crosshair labels
            val priceLabel = dev.kbwallet.app.chart.presentation.util.ChartFormatters.formatPrice(c.close)
            val timeLabel = dev.kbwallet.app.chart.presentation.util.ChartFormatters.formatTimeShort(c.openTime)
            
            // Price Tag
            drawRect(
                color = Color.White,
                topLeft = Offset(chartWidth, closeY - 8.dp.toPx()),
                size = Size(rightMargin, 16.dp.toPx())
            )
            drawText(
                textMeasurer = textMeasurer,
                text = priceLabel,
                style = axisTextStyle.copy(color = Color.Black, fontWeight = FontWeight.Bold),
                topLeft = Offset(chartWidth + 2.dp.toPx(), closeY - 6.dp.toPx())
            )

            // Time Tag
            drawRect(
                color = Color.White,
                topLeft = Offset(cx - 25.dp.toPx(), chartHeight),
                size = Size(50.dp.toPx(), 16.dp.toPx())
            )
            drawText(
                textMeasurer = textMeasurer,
                text = timeLabel,
                style = axisTextStyle.copy(color = Color.Black, fontWeight = FontWeight.Bold),
                topLeft = Offset(cx - 20.dp.toPx(), chartHeight + 2.dp.toPx())
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
