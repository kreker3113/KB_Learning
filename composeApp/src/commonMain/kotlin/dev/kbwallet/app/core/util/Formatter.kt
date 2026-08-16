package dev.kbwallet.app.core.util

import kotlin.math.abs
import kotlin.math.round

expect fun formatFiat(amount: Double, showDecimal: Boolean = true): String

expect fun formatCoinUnit(amount: Double, symbol: String): String

expect fun formatPercentage(amount: Double): String

/**
 * Plain decimal string for a Double, safe to feed back into a numeric text
 * field the user can keep editing (e.g. a percentage/MAX quick-amount button
 * populating a trade amount field). Kotlin's default `Double.toString()`
 * switches to scientific notation for very small/large magnitudes (e.g.
 * "3.0E-8" for a tiny crypto balance), which reads as junk in a text field —
 * this always returns a plain "123.456" style string, KMP-safe (no
 * String.format, matching this codebase's other manual formatters).
 */
fun Double.toPlainAmountString(maxDecimals: Int = 8): String {
    if (this == 0.0 || this.isNaN()) return "0"
    val negative = this < 0
    val abs = abs(this)
    var factor = 1.0
    repeat(maxDecimals) { factor *= 10 }
    val rounded = round(abs * factor) / factor
    val intPart = rounded.toLong()
    var fracRemainder = rounded - intPart
    val fracDigits = StringBuilder()
    repeat(maxDecimals) {
        fracRemainder *= 10
        val digit = fracRemainder.toInt().coerceIn(0, 9)
        fracDigits.append(digit)
        fracRemainder -= digit
    }
    val trimmedFrac = fracDigits.toString().trimEnd('0')
    return buildString {
        if (negative) append('-')
        append(intPart)
        if (trimmedFrac.isNotEmpty()) {
            append('.')
            append(trimmedFrac)
        }
    }
}