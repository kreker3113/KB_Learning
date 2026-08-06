package dev.kbwallet.app.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kblearning.composeapp.generated.resources.Res
import kblearning.composeapp.generated.resources.profile_edit_button
import kblearning.composeapp.generated.resources.profile_menu_help_subtitle
import kblearning.composeapp.generated.resources.profile_menu_help_title
import kblearning.composeapp.generated.resources.profile_menu_notifications_subtitle
import kblearning.composeapp.generated.resources.profile_menu_notifications_title
import kblearning.composeapp.generated.resources.profile_menu_personal_info_subtitle
import kblearning.composeapp.generated.resources.profile_menu_personal_info_title
import kblearning.composeapp.generated.resources.profile_menu_pnl_subtitle
import kblearning.composeapp.generated.resources.profile_menu_pnl_title
import kblearning.composeapp.generated.resources.profile_menu_security_subtitle
import kblearning.composeapp.generated.resources.profile_menu_security_title
import kblearning.composeapp.generated.resources.profile_stat_days_active
import kblearning.composeapp.generated.resources.profile_stat_total_trades
import kblearning.composeapp.generated.resources.profile_stat_win_rate
import kblearning.composeapp.generated.resources.profile_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToPnL: () -> Unit = {},
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                text = stringResource(Res.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Avatar + Info ──
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.avatarInitial,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onNavigateToEditProfile,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                ) {
                    Text(
                        text = stringResource(Res.string.profile_edit_button),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // ── Stats Row ──
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileStatCard(
                    title = stringResource(Res.string.profile_stat_total_trades),
                    value = state.totalTrades.toString(),
                    modifier = Modifier.weight(1f),
                )
                ProfileStatCard(
                    title = stringResource(Res.string.profile_stat_win_rate),
                    value = state.winRate,
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary,
                )
                ProfileStatCard(
                    title = stringResource(Res.string.profile_stat_days_active),
                    value = state.daysActive,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Menu ──
        item {
            ProfileMenuOption(
                icon = Icons.Default.Person,
                title = stringResource(Res.string.profile_menu_personal_info_title),
                subtitle = stringResource(Res.string.profile_menu_personal_info_subtitle),
                onClick = onNavigateToEditProfile,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.Notifications,
                title = stringResource(Res.string.profile_menu_notifications_title),
                subtitle = stringResource(Res.string.profile_menu_notifications_subtitle),
                onClick = onNavigateToNotifications,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.Lock,
                title = stringResource(Res.string.profile_menu_security_title),
                subtitle = stringResource(Res.string.profile_menu_security_subtitle),
                onClick = onNavigateToSecurity,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.TrendingUp,
                title = stringResource(Res.string.profile_menu_pnl_title),
                subtitle = stringResource(Res.string.profile_menu_pnl_subtitle),
                onClick = onNavigateToPnL,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = stringResource(Res.string.profile_menu_help_title),
                subtitle = stringResource(Res.string.profile_menu_help_subtitle),
                onClick = onNavigateToHelp,
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun ProfileMenuOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
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
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp),
        )
    }
}
