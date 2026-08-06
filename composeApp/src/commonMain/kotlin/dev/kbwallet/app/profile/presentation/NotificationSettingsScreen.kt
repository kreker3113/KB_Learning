package dev.kbwallet.app.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kblearning.composeapp.generated.resources.Res
import kblearning.composeapp.generated.resources.action_back
import kblearning.composeapp.generated.resources.notif_email_subtitle
import kblearning.composeapp.generated.resources.notif_email_title
import kblearning.composeapp.generated.resources.notif_news_subtitle
import kblearning.composeapp.generated.resources.notif_news_title
import kblearning.composeapp.generated.resources.notif_price_alerts_subtitle
import kblearning.composeapp.generated.resources.notif_price_alerts_title
import kblearning.composeapp.generated.resources.notif_push_subtitle
import kblearning.composeapp.generated.resources.notif_push_title
import kblearning.composeapp.generated.resources.notif_trade_confirmations_subtitle
import kblearning.composeapp.generated.resources.notif_trade_confirmations_title
import kblearning.composeapp.generated.resources.notifications_title
import kblearning.composeapp.generated.resources.section_general
import kblearning.composeapp.generated.resources.section_other
import kblearning.composeapp.generated.resources.section_trading
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.notifications_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── GENERAL ──
            item {
                SectionHeader(stringResource(Res.string.section_general))
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(Res.string.notif_push_title),
                    subtitle = stringResource(Res.string.notif_push_subtitle),
                    checked = state.pushNotifications,
                    onToggle = { viewModel.togglePushNotifications() },
                )
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Email,
                    title = stringResource(Res.string.notif_email_title),
                    subtitle = stringResource(Res.string.notif_email_subtitle),
                    checked = state.emailNotifications,
                    onToggle = { viewModel.toggleEmailNotifications() },
                )
            }

            // ── TRADING ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(stringResource(Res.string.section_trading))
            }
            item {
                ToggleItem(
                    icon = Icons.Default.TrendingUp,
                    title = stringResource(Res.string.notif_price_alerts_title),
                    subtitle = stringResource(Res.string.notif_price_alerts_subtitle),
                    checked = state.priceAlerts,
                    onToggle = { viewModel.togglePriceAlerts() },
                )
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Receipt,
                    title = stringResource(Res.string.notif_trade_confirmations_title),
                    subtitle = stringResource(Res.string.notif_trade_confirmations_subtitle),
                    checked = state.tradeConfirmations,
                    onToggle = { viewModel.toggleTradeConfirmations() },
                )
            }

            // ── OTHER ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(stringResource(Res.string.section_other))
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Newspaper,
                    title = stringResource(Res.string.notif_news_title),
                    subtitle = stringResource(Res.string.notif_news_subtitle),
                    checked = state.newsUpdates,
                    onToggle = { viewModel.toggleNewsUpdates() },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun ToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = Color.Gray,
            ),
        )
    }
}
