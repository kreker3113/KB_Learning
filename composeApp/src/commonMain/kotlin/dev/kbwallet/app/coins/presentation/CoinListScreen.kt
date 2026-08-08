package dev.kbwallet.app.coins.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoinListScreen(
    onCoinClicked: (String) -> Unit,
    onChartRequested: (String, String) -> Unit,
    onBack: () -> Unit = {},
) {
    val coinsListViewModel = koinViewModel<CoinsListViewModel>()
    val state by coinsListViewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Header ──
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = strings.actionBack,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.coinsListTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        items(state.coins) { coin ->
            CoinListItem(
                coin = coin,
                onCoinLongPressed = { coinId ->
                    onChartRequested(coinId, coin.name)
                },
                onCoinClicked = onCoinClicked,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CoinListItem(
    coin: UiCoinListItem,
    onCoinLongPressed: (String) -> Unit,
    onCoinClicked: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onLongClick = { onCoinLongPressed(coin.id) },
                onClick = { onCoinClicked(coin.id) }
            )
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF2A2A2A), CircleShape)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = coin.iconUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.clip(CircleShape).size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = coin.symbol,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = coin.formattedPrice,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = coin.formattedChange,
                style = MaterialTheme.typography.bodySmall,
                color = if (coin.isPositive)
                    LocalKBLearningColorsPalette.current.profitGreen
                else
                    LocalKBLearningColorsPalette.current.lossRed,
            )
        }
    }
}
