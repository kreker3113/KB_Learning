package dev.kbwallet.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.kbwallet.app.analytics.presentation.PnLScreen
import dev.kbwallet.app.chart.presentation.CryptoChartScreen
import dev.kbwallet.app.coins.presentation.CoinListScreen
import dev.kbwallet.app.core.biometric.BiometricScreen
import dev.kbwallet.app.core.navigation.ActiveOrders
import dev.kbwallet.app.core.navigation.Biometric
import dev.kbwallet.app.core.navigation.Buy
import dev.kbwallet.app.core.navigation.Coins
import dev.kbwallet.app.core.navigation.CryptoChart
import dev.kbwallet.app.core.navigation.Dashboard
import dev.kbwallet.app.core.navigation.EditProfile
import dev.kbwallet.app.core.navigation.HelpSupport
import dev.kbwallet.app.core.navigation.History
import dev.kbwallet.app.core.navigation.LanguageSettings
import dev.kbwallet.app.core.navigation.NotificationSettings
import dev.kbwallet.app.core.navigation.PnLAnalytics
import dev.kbwallet.app.core.navigation.Portfolio
import dev.kbwallet.app.core.navigation.Profile
import dev.kbwallet.app.core.navigation.SecuritySettings
import dev.kbwallet.app.core.navigation.Sell
import dev.kbwallet.app.core.navigation.Simulator
import dev.kbwallet.app.core.navigation.Library
import dev.kbwallet.app.core.navigation.Topic
import dev.kbwallet.app.core.i18n.AppStrings
import dev.kbwallet.app.core.i18n.ProvideAppLanguage
import dev.kbwallet.app.core.i18n.appStrings

import dev.kbwallet.app.dashboard.presentation.DashboardScreen
import dev.kbwallet.app.library.presentation.LibraryScreen
import dev.kbwallet.app.library.presentation.LibraryTopicScreen
import dev.kbwallet.app.history.presentation.HistoryScreen
import dev.kbwallet.app.portfolio.presentation.PortfolioScreen
import dev.kbwallet.app.profile.presentation.EditProfileScreen
import dev.kbwallet.app.profile.presentation.HelpSupportScreen
import dev.kbwallet.app.profile.presentation.LanguageSettingsScreen
import dev.kbwallet.app.profile.presentation.NotificationSettingsScreen
import dev.kbwallet.app.profile.presentation.ProfileScreen
import dev.kbwallet.app.profile.presentation.SecuritySettingsScreen
import dev.kbwallet.app.theme.KBLearningTheme
import dev.kbwallet.app.trade.presentation.buy.BuyScreen
import dev.kbwallet.app.trade.presentation.sell.SellScreen
import dev.kbwallet.app.simulator.presentation.SimulatorScreen

import org.jetbrains.compose.ui.tooling.preview.Preview

// ── Bottom navigation tab definition ──
private enum class BottomTab(
    val icon: ImageVector,
) {
    Dashboard(Icons.Default.Home),
    Portfolio(Icons.Default.PieChart),
    History(Icons.Default.History),
    Profile(Icons.Default.Person),
}

private fun BottomTab.label(strings: AppStrings): String = when (this) {
    BottomTab.Dashboard -> strings.navDashboard
    BottomTab.Portfolio -> strings.navPortfolio
    BottomTab.History -> strings.navHistory
    BottomTab.Profile -> strings.navProfile
}

// Routes where the bottom bar should be visible
private val bottomBarRoutes = setOf(
    Dashboard::class,
    Portfolio::class,
    History::class,
    Profile::class,
)

@Composable
@Preview
fun App() {
    ProvideAppLanguage {
    val navController: NavHostController = rememberNavController()
    KBLearningTheme {
        NavHost(
            navController = navController,
            startDestination = Biometric,
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Biometric entry ──
            composable<Biometric> {
                BiometricScreen {
                    navController.navigate(Portfolio) {
                        popUpTo(Biometric) { inclusive = true }
                    }
                }
            }

            // ── Main scaffold with tabs ──
            composable<Dashboard> {
                MainScaffold(navController = navController)
            }
            composable<Portfolio> {
                MainScaffold(navController = navController)
            }
            composable<History> {
                MainScaffold(navController = navController)
            }
            composable<Profile> {
                MainScaffold(navController = navController)
            }

            // ── Secondary screens (no bottom bar) ──
            composable<Coins> {
                CoinListScreen(
                    onCoinClicked = { coinId ->
                        navController.navigate(Buy(coinId))
                    },
                    onChartRequested = { coinId, coinName ->
                        navController.navigate(CryptoChart(coinId, coinName))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Buy> { navBackStackEntry ->
                val coinId: String = navBackStackEntry.toRoute<Buy>().coinId
                BuyScreen(
                    coinId = coinId,
                    navigateToPortfolio = {
                        navController.navigate(Portfolio) {
                            popUpTo(Portfolio) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Sell> { navBackStackEntry ->
                val coinId: String = navBackStackEntry.toRoute<Sell>().coinId
                SellScreen(
                    coinId = coinId,
                    navigateToPortfolio = {
                        navController.navigate(Portfolio) {
                            popUpTo(Portfolio) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<CryptoChart> { navBackStackEntry ->
                val route = navBackStackEntry.toRoute<CryptoChart>()
                CryptoChartScreen(
                    coinId = route.coinId,
                    coinName = route.coinName,
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Profile sub-screens ──
            composable<EditProfile> {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<NotificationSettings> {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<SecuritySettings> {
                SecuritySettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<HelpSupport> {
                HelpSupportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<LanguageSettings> {
                LanguageSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Trading Simulator additions ──
            composable<PnLAnalytics> {
                PnLScreen(onBack = { navController.popBackStack() })
            }
            composable<ActiveOrders> {
                // Active orders screen (limit orders overview)
                // Navigate back
                navController.popBackStack()
            }
            composable<Simulator> {
                SimulatorScreen(onBack = { navController.popBackStack() })
            }

            // ── Knowledge library ──
            composable<Library> {
                LibraryScreen(
                    onBack = { navController.popBackStack() },
                    onTopicClicked = { topicId -> navController.navigate(Topic(topicId)) },
                )
            }
            composable<Topic> { navBackStackEntry ->
                val topicId: String = navBackStackEntry.toRoute<Topic>().topicId
                LibraryTopicScreen(
                    topicId = topicId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
    }
}

@Composable
private fun MainScaffold(navController: NavHostController) {
    val strings = appStrings()
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.let { dest ->
        bottomBarRoutes.any { dest.hasRoute(it) }
    } ?: true

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    BottomTab.entries.forEach { tab ->
                        val selected = when (tab) {
                            BottomTab.Dashboard -> currentDestination?.hasRoute(Dashboard::class) == true
                            BottomTab.Portfolio -> currentDestination?.hasRoute(Portfolio::class) == true
                            BottomTab.History -> currentDestination?.hasRoute(History::class) == true
                            BottomTab.Profile -> currentDestination?.hasRoute(Profile::class) == true
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                when (tab) {
                                    BottomTab.Dashboard -> innerNavController.navigate(Dashboard)
                                    BottomTab.Portfolio -> innerNavController.navigate(Portfolio)
                                    BottomTab.History -> innerNavController.navigate(History)
                                    BottomTab.Profile -> innerNavController.navigate(Profile)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label(strings),
                                )
                            },
                            label = { Text(text = tab.label(strings)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            ),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Dashboard,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable<Dashboard> {
                DashboardScreen(
                    onDiscoverCoinsClicked = {
                        navController.navigate(Coins)
                    },
                    onCoinItemClicked = { coinId ->
                        navController.navigate(Sell(coinId))
                    },
                    onSimulatorClicked = {
                        navController.navigate(Simulator)
                    },
                    onLibraryClicked = {
                        navController.navigate(Library)
                    },
                )
            }
            composable<Portfolio> {
                PortfolioScreen(
                    onCoinItemClicked = { coinId ->
                        navController.navigate(Sell(coinId))
                    },
                    onDiscoverCoinsClicked = {
                        navController.navigate(Coins)
                    },
                )
            }
            composable<History> {
                HistoryScreen()
            }
            composable<Profile> {
                ProfileScreen(
                    onNavigateToEditProfile = {
                        navController.navigate(EditProfile)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(NotificationSettings)
                    },
                    onNavigateToSecurity = {
                        navController.navigate(SecuritySettings)
                    },
                    onNavigateToHelp = {
                        navController.navigate(HelpSupport)
                    },
                    onNavigateToPnL = {
                        navController.navigate(PnLAnalytics)
                    },
                    onNavigateToLanguage = {
                        navController.navigate(LanguageSettings)
                    },
                )
            }
        }
    }
}
