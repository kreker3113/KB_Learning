package dev.kbwallet.app.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import dev.kbwallet.app.theme.component.StatCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen() {
    val viewModel = koinViewModel<HistoryViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    } else {
        HistoryContent(state = state)
    }
}

@Composable
private fun HistoryContent(state: HistoryState) {
    val strings = appStrings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ──
        item {
            Text(
                text = strings.historyTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Stats Row ──
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = strings.historyStatTotalTrades,
                    value = state.totalTrades.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = strings.historyStatTotalBuy,
                    value = state.totalBuy.toString(),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary,
                )
                StatCard(
                    title = strings.historyStatTotalSell,
                    value = state.totalSell.toString(),
                    modifier = Modifier.weight(1f),
                    valueColor = LocalKBLearningColorsPalette.current.lossRed,
                )
            }
        }

        // ── Section title ──
        item {
            Text(
                text = strings.historyRecentTransactions,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Transactions or empty state ──
        if (state.transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.historyEmptyTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.historyEmptySubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                    }
                }
            }
        } else {
            items(
                state.transactions,
                key = { it.id }
            ) { transaction ->
                Column {
                    TransactionItem(transaction = transaction)
                    if (transaction.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📝 ${transaction.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        if (transaction.tags.isNotEmpty()) {
                            Text(
                                text = transaction.tags.split(",").joinToString(" ") { "#$it" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: TransactionUiModel) {
    val strings = appStrings()
    val isBuy = transaction.type == "BUY"
    val accentColor = if (isBuy)
        LocalKBLearningColorsPalette.current.profitGreen
    else
        LocalKBLearningColorsPalette.current.lossRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // ── Top row ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            accentColor.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if (isBuy) strings.historyBuyLabel else strings.historySellLabel} ${transaction.coinName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "${transaction.formattedDate}, ${transaction.formattedTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isBuy) "-" else "+"}${transaction.formattedFiatAmount}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                    )
                    Text(
                        text = transaction.formattedUnitAmount,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )

            // ── Bottom row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.historyPriceLabel(transaction.formattedPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = transaction.status,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
