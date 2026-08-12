package dev.kbwallet.app.chart.presentation.util

import kotlin.math.abs
import kotlin.math.round
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object ChartFormatters {

    fun formatPrice(price: Double): String {
        val absPrice = kotlin.math.abs(price)
        return when {
            absPrice >= 10_000 -> formatWithCommas(price, 0)
            absPrice >= 1_000 -> formatWithCommas(price, 0)
            absPrice >= 1.0   -> roundToString(price, 2)
            absPrice >= 0.01  -> roundToString(price, 4)
            absPrice >= 0.0001 -> roundToString(price, 6)
            else -> roundToString(price, 8)
        }
    }

    private fun formatWithCommas(value: Double, decimals: Int): String {
        val factor = pow10(decimals)
        val rounded = kotlin.math.round(value * factor) / factor
        val intPart = rounded.toLong()
        val sign = if (value < 0) "-" else ""
        val absInt = kotlin.math.abs(intPart)
        val str = absInt.toString().reversed().chunked(3).joinToString(",").reversed()
        val frac = if (decimals > 0) {
            val fracVal = kotlin.math.abs(rounded - intPart)
            "." + (fracVal * factor).toLong().toString().padStart(decimals, '0')
        } else ""
        return "$sign$str$frac"
    }

    private fun roundToString(value: Double, decimals: Int): String {
        val factor = pow10(decimals)
        val rounded = kotlin.math.round(value * factor) / factor
        val intPart = rounded.toLong()
        val fracPart = kotlin.math.abs(rounded - intPart)
        val fracStr = if (decimals > 0) {
            "." + (fracPart * factor).toLong().toString().padStart(decimals, '0')
        } else ""
        return "$intPart$fracStr"
    }

    fun formatVolume(volume: Double): String = when {
        volume >= 1_000_000_000 -> roundToString(volume / 1_000_000_000, 1) + "B"
        volume >= 1_000_000     -> roundToString(volume / 1_000_000, 1) + "M"
        volume >= 1_000         -> roundToString(volume / 1_000, 1) + "K"
        else -> roundToString(volume, 0)
    }

    fun formatTimeShort(timestampMs: Long): String {
        if (timestampMs == 0L) return ""
        val dt = Instant.fromEpochMilliseconds(timestampMs).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.dayOfMonth.toString().padStart(2, '0')}.${dt.monthNumber.toString().padStart(2, '0')}"
    }

    fun formatDateTime(timestampMs: Long): String {
        if (timestampMs == 0L) return ""
        val dt = Instant.fromEpochMilliseconds(timestampMs).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.dayOfMonth.toString().padStart(2, '0')}.${dt.monthNumber.toString().padStart(2, '0')} ${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    }
}

private fun pow10(n: Int): Double {
    var r = 1.0; repeat(n) { r *= 10.0 }; return r
}

fun Double.formatPriceString(): String = ChartFormatters.formatPrice(this)
fun Double.formatVolumeString(): String = ChartFormatters.formatVolume(this)
