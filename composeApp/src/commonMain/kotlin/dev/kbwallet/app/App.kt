package dev.kbwallet.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.kbwallet.app.biometric.BiometricScreen
import dev.kbwallet.app.coins.presentation.CoinListScreen
import dev.kbwallet.app.core.navigation.Buy
import dev.kbwallet.app.core.navigation.Coins
import dev.kbwallet.app.core.navigation.Portfolio
import dev.kbwallet.app.core.navigation.Sell
import dev.kbwallet.app.portfolio.presentation.PortfolioScreen
import dev.kbwallet.app.theme.KBWalletTheme
import dev.kbwallet.app.trade.presentation.buy.BuyScreen
import dev.kbwallet.app.trade.presentation.sell.SellScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController: NavHostController = rememberNavController()
    KBWalletTheme {
        NavHost(
            navController = navController,
            startDestination = Portfolio,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Portfolio> {
                PortfolioScreen(
                    onCoinItemClicked = { coinId ->
                        navController.navigate(Sell(coinId))
                    },
                    onDiscoverCoinsClicked = {
                        navController.navigate(Coins)
                    }
                )
            }

            composable<Coins> {
                CoinListScreen { coinId ->
                    navController.navigate(Buy(coinId))
                }
            }

            composable<Buy> { navBackStackEntry ->
                val coinId: String = navBackStackEntry.toRoute<Buy>().coinId
                BuyScreen(
                    coinId = coinId,
                    navigateToPortfolio = {
                        navController.navigate(Portfolio) {
                            popUpTo(Portfolio) { inclusive = true }
                        }
                    }
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
                    }
                )
            }

        }
    }
}