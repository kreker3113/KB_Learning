package dev.kbwallet.app.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared "stat tile" — the small metric cards used on Dashboard, History,
 * Profile, P&L Analytics, and the Simulator (balance, win rate, etc.).
 *
 * These used to be six near-identical, hand-copied composables (one per
 * screen) that had each drifted to slightly different padding/corner-radius/
 * font-size values, which is why the cards didn't line up visually between
 * screens (and sometimes even within the same row, since nothing forced a
 * shared height). One definition, one fixed height per size, used
 * everywhere instead.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    size: StatCardSize = StatCardSize.Regular,
    monospaceValue: Boolean = false,
) {
    Box(
        modifier = modifier
            .height(size.height)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(size.cornerRadius))
            .padding(size.padding)
    ) {
        Column {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = size.titleFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                fontSize = size.valueFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = if (monospaceValue) FontFamily.Monospace else FontFamily.Default,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

enum class StatCardSize(
    val height: androidx.compose.ui.unit.Dp,
    val padding: androidx.compose.ui.unit.Dp,
    val cornerRadius: androidx.compose.ui.unit.Dp,
    val titleFontSize: androidx.compose.ui.unit.TextUnit,
    val valueFontSize: androidx.compose.ui.unit.TextUnit,
) {
    /** Dashboard, History, Profile, P&L — three cards per row. */
    Regular(height = 72.dp, padding = 16.dp, cornerRadius = 16.dp, titleFontSize = 12.sp, valueFontSize = 18.sp),

    /** Simulator's denser grids (up to six cards across two rows). */
    Compact(height = 56.dp, padding = 10.dp, cornerRadius = 12.dp, titleFontSize = 11.sp, valueFontSize = 15.sp),
}
