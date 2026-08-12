package dev.kbwallet.app.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
 * shared height).
 *
 * The height itself is intentionally NOT a fixed dp constant — an earlier
 * version of this component pinned a guessed height, which clipped the
 * bottom of the value text once real font metrics (line height, not just
 * the nominal sp size) were taken into account. Instead this fills the
 * height of its parent, and callers put their Row in
 * `Modifier.height(IntrinsicSize.Max)` so every card in the row stretches to
 * match whichever one actually needs the most room — correct regardless of
 * font/locale/DPI instead of a magic number that happens to fit today.
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
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(size.cornerRadius))
            .padding(size.padding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(size.titleValueGap)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = size.titleFontSize,
                maxLines = 2,
                lineHeight = size.titleFontSize * 1.2f, // Ensure readable line height if it wraps
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
    val padding: Dp,
    val cornerRadius: Dp,
    val titleValueGap: Dp,
    val titleFontSize: TextUnit,
    val valueFontSize: TextUnit,
) {
    /** Dashboard, History, Profile, P&L — three cards per row. */
    Regular(padding = 16.dp, cornerRadius = 16.dp, titleValueGap = 4.dp, titleFontSize = 12.sp, valueFontSize = 18.sp),

    /** Simulator's denser grids (up to six cards across two rows). */
    Compact(padding = 10.dp, cornerRadius = 12.dp, titleValueGap = 2.dp, titleFontSize = 11.sp, valueFontSize = 15.sp),
}
