package dev.kbwallet.app.simulator.presentation

import app.cash.turbine.test
import dev.kbwallet.app.coins.data.remote.FakeCoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.core.util.toUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatorViewModelTest {

    private lateinit var viewModel: SimulatorViewModel
    private lateinit var dataSource: FakeCoinsRemoteDataSource
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataSource = FakeCoinsRemoteDataSource()
        viewModel = SimulatorViewModel(dataSource)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCoins fetches available coins`() = runTest {
        viewModel.loadCoins()

        viewModel.state.test {
            val state = awaitItem()
            if (state.availableCoins.isEmpty() && state.error == null) {
                val nextState = awaitItem()
                assertTrue(nextState.availableCoins.isNotEmpty())
                assertEquals("Bitcoin", nextState.availableCoins[0].name)
            } else {
                assertTrue(state.availableCoins.isNotEmpty())
                assertEquals("Bitcoin", state.availableCoins[0].name)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCoin loads klines`() = runTest {
        val dummyCoin = Coin("1", "Bitcoin", "BTC", "")
        viewModel.selectCoin(dummyCoin)

        viewModel.state.test {
            val state = awaitItem()
            if (state.candles.isEmpty() && state.error == null) {
                val nextState = awaitItem()
                assertEquals(10, nextState.candles.size)
                assertEquals(10000.0, nextState.cashBalance)
                assertEquals(10000.0, nextState.equity)
            } else {
                assertEquals(10, state.candles.size)
                assertEquals(10000.0, state.cashBalance)
                assertEquals(10000.0, state.equity)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openPosition decreases cash balance and adds position`() = runTest {
        val dummyCoin = Coin("1", "Bitcoin", "BTC", "")
        viewModel.selectCoin(dummyCoin)

        viewModel.onOrderAmountChanged("1000")
        viewModel.onOrderSideChanged(OrderSideInput.LONG)
        viewModel.onOrderLeverageChanged("1")
        viewModel.openPosition()

        viewModel.state.test {
            val state = awaitItem()
            // Could be any intermediate state if emissions are rapid, but eventually:
            if (state.positions.isEmpty()) {
                val nextState = awaitItem()
                assertEquals(1, nextState.positions.size)
                assertEquals(9000.0, nextState.cashBalance)
            } else {
                assertEquals(1, state.positions.size)
                assertEquals(9000.0, state.cashBalance)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
