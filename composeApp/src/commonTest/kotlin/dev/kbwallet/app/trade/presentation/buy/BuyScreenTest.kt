package dev.kbwallet.app.trade.presentation.buy

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kblearning.composeapp.generated.resources.Res
import kblearning.composeapp.generated.resources.error_unknown
import dev.kbwallet.app.trade.presentation.common.TradeScreen
import dev.kbwallet.app.trade.presentation.common.TradeState
import dev.kbwallet.app.trade.presentation.common.TradeType
import dev.kbwallet.app.trade.presentation.common.UiTradeCoinItem
import kotlin.test.Test

class BuyScreenTest {

    @OptIn(ExperimentalTestApi::class)
    @kotlin.test.Ignore
    @Test
    fun checkSubmitButtonLabelChangesWithTradeType() = runComposeUiTest {
        val state = TradeState(
            coin = UiTradeCoinItem(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                iconUrl = "url",
                price = 50000.0
            )
        )

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.BUY,
                onAmountChange = {},
                onSubmitClicked = {},
                onToggleMode = {},
                onPercentageClicked = {}
            )
        }

        onNodeWithText("Sell").assertDoesNotExist()
        onNodeWithText("Buy").assertExists()
        onNodeWithText("Buy").assertIsDisplayed()

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.SELL,
                onAmountChange = {},
                onSubmitClicked = {},
                onToggleMode = {},
                onPercentageClicked = {}
            )
        }

        onNodeWithText("Buy").assertDoesNotExist()
        onNodeWithText("Sell").assertExists()
        onNodeWithText("Sell").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @kotlin.test.Ignore
    @Test
    fun checkIfCoinNameShowProperlyInBuy() = runComposeUiTest {
        val state = TradeState(
            coin = UiTradeCoinItem(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                iconUrl = "url",
                price = 50000.0
            )
        )

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.BUY,
                onAmountChange = {},
                onSubmitClicked = {},
                onToggleMode = {},
                onPercentageClicked = {}
            )
        }

        onNodeWithTag("trade_screen_coin_name").assertExists()
        onNodeWithTag("trade_screen_coin_name").assertTextEquals("Bitcoin")
    }

    @OptIn(ExperimentalTestApi::class)
    @kotlin.test.Ignore
    @Test
    fun checkErrorIsShownProperly() = runComposeUiTest {
        val state = TradeState(
            coin = UiTradeCoinItem(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                iconUrl = "url",
                price = 50000.0
            ),
            error = Res.string.error_unknown
        )

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.BUY,
                onAmountChange = {},
                onSubmitClicked = {},
                onToggleMode = {},
                onPercentageClicked = {}
            )
        }

        onNodeWithTag("trade_error").assertExists()
        onNodeWithTag("trade_error").assertIsDisplayed()
    }
}