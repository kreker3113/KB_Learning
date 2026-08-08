package dev.kbwallet.app.trade.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import dev.kbwallet.app.trade.presentation.common.component.rememberCurrencyVisualTransformation
import org.jetbrains.compose.resources.stringResource

@Composable
fun TradeScreen(
    state: TradeState,
    tradeType: TradeType,
    onAmountChange: (String) -> Unit,
    onSubmitClicked: () -> Unit,
    onToggleMode: () -> Unit,
    onBack: () -> Unit = {},
) {
    val strings = appStrings()
    val accentColor = when (tradeType) {
        TradeType.BUY -> MaterialTheme.colorScheme.primary
        TradeType.SELL -> LocalKBLearningColorsPalette.current.lossRed
    }
    val buttonTextColor = when (tradeType) {
        TradeType.BUY -> MaterialTheme.colorScheme.onPrimary
        TradeType.SELL -> Color.White
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Back ──
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(36.dp),
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = strings.actionBack,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            // ── Coin Chip ──
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                AsyncImage(
                    model = state.coin?.iconUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(4.dp).clip(CircleShape).size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.coin?.name ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("trade_screen_coin_name"),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Mode Toggle (Fiat / Coin) ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (!state.isAmountInUnits) FontWeight.Bold else FontWeight.Normal,
                    color = if (!state.isAmountInUnits) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Switch(
                    checked = state.isAmountInUnits,
                    onCheckedChange = { onToggleMode() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Text(
                    text = state.coin?.symbol ?: strings.tradeCoinFallback,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (state.isAmountInUnits) FontWeight.Bold else FontWeight.Normal,
                    color = if (state.isAmountInUnits) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Title ──
            Text(
                text = when (tradeType) {
                    TradeType.BUY -> if (state.isAmountInUnits) strings.tradeCoinAmountLabel else strings.tradeBuyAmountLabel
                    TradeType.SELL -> if (state.isAmountInUnits) strings.tradeCoinAmountLabel else strings.tradeSellAmountLabel
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )

            // ── Amount Input ──
            CenteredDollarTextField(
                amountText = state.amount,
                onAmountChange = onAmountChange
            )

            // ── Fiat equivalent (when in coin mode) ──
            if (state.isAmountInUnits && state.fiatEquivalent.isNotEmpty()) {
                Text(
                    text = state.fiatEquivalent,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            // ── Available Balance ──
            Text(
                text = state.availableAmount,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(4.dp)
            )

            // ── Error ──
            if (state.error != null) {
                Text(
                    text = stringResource(state.error),
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalKBLearningColorsPalette.current.lossRed,
                    modifier = Modifier.padding(4.dp).testTag("trade_error")
                )
            }
        }

        // ── Action Button ──
        Button(
            onClick = onSubmitClicked,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = buttonTextColor,
            ),
            contentPadding = PaddingValues(horizontal = 64.dp, vertical = 14.dp),
        ) {
            Text(
                text = when (tradeType) {
                    TradeType.BUY -> strings.tradeBuyButton
                    TradeType.SELL -> strings.tradeSellButton
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun CenteredDollarTextField(
    modifier: Modifier = Modifier,
    amountText: String,
    onAmountChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val currencyVisualTransformation = rememberCurrencyVisualTransformation()

    val displayText = amountText.trimStart('$')

    BasicTextField(
        value = displayText,
        onValueChange = { newValue ->
            val cleaned = newValue.filter { it.isDigit() || it == '.' }
                .let { str ->
                    val dotIndex = str.indexOf('.')
                    if (dotIndex >= 0) {
                        str.take(dotIndex + 1) + str.drop(dotIndex + 1).filter { it.isDigit() }
                    } else str
                }

            if (cleaned.isEmpty()) {
                onAmountChange("")
                return@BasicTextField
            }

            val num = cleaned.toDoubleOrNull() ?: return@BasicTextField
            if (num in 0.0..10000.0) {
                onAmountChange(cleaned)
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .padding(16.dp),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(56.dp).wrapContentWidth()
            ) {
                innerTextField()
            }
        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = currencyVisualTransformation,
    )
}

enum class TradeType {
    BUY, SELL
}
