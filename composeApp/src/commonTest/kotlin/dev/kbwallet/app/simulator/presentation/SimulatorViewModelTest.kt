package dev.kbwallet.app.simulator.presentation

import dev.kbwallet.app.coins.data.FakeCoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.simulator.domain.ExitReason
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Regression coverage for SimulatorViewModel — this ViewModel previously
 * shipped two real money-math bugs (uncapped leveraged losses with no
 * liquidation, and equity dropping by a position's margin the instant it
 * opened) that were only caught by manually running the app. Also covers
 * the "stale candles on a failed coin switch" bug found in code review on
 * the error-retry-ui PR (candles must be cleared before a new fetch starts).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SimulatorViewModelTest {

    private lateinit var coinsRemoteDataSource: FakeCoinsRemoteDataSource
    private lateinit var viewModel: SimulatorViewModel

    private val testCoin = Coin(
        id = "bitcoin",
        name = "Bitcoin",
        symbol = "BTC",
        iconUrl = "https://fake.url/btc.png",
    )

    @BeforeTest
    fun setup() {
        // viewModelScope resolves to Dispatchers.Main.immediate; swap in a
        // deterministic test dispatcher so launched coroutines run synchronously.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coinsRemoteDataSource = FakeCoinsRemoteDataSource()
        viewModel = SimulatorViewModel(coinsRemoteDataSource)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCoins success populates availableCoins`() = runTest {
        coinsRemoteDataSource.coinsResult = Result.Success(listOf(FakeCoinsRemoteDataSource.fakeCoinMarketDto))

        viewModel.loadCoins()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.availableCoins.size)
        assertEquals("bitcoin", state.availableCoins.first().id)
    }

    @Test
    fun `loadCoins error surfaces FAILED_TO_LOAD_COINS`() = runTest {
        coinsRemoteDataSource.coinsResult = Result.Error(DataError.Remote.SERVER)

        viewModel.loadCoins()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(SimulatorErrorType.FAILED_TO_LOAD_COINS, state.error)
    }

    @Test
    fun `selectCoin with too little history sets NOT_ENOUGH_DATA and clears candles`() = runTest {
        coinsRemoteDataSource.ohlcResult = Result.Success(FakeCoinsRemoteDataSource.fakeOhlcCandles(5))

        viewModel.selectCoin(testCoin)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(SimulatorErrorType.NOT_ENOUGH_DATA, state.error)
        assertTrue(state.candles.isEmpty())
    }

    @Test
    fun `selectCoin with a failed fetch clears any previously loaded candles`() = runTest {
        // First selection succeeds and populates candles...
        coinsRemoteDataSource.ohlcResult = Result.Success(FakeCoinsRemoteDataSource.fakeOhlcCandles(20))
        viewModel.selectCoin(testCoin)
        assertTrue(viewModel.state.value.candles.isNotEmpty())

        // ...then switching to a coin whose fetch fails must not leave the
        // first coin's candles on screen (see class doc).
        coinsRemoteDataSource.ohlcResult = Result.Error(DataError.Remote.SERVER)
        val otherCoin = Coin(id = "ethereum", name = "Ethereum", symbol = "ETH", iconUrl = "https://fake.url/eth.png")
        viewModel.selectCoin(otherCoin)

        val state = viewModel.state.value
        assertEquals(SimulatorErrorType.FAILED_TO_LOAD_DATA, state.error)
        assertTrue(state.candles.isEmpty())
    }

    @Test
    fun `selectCoin with enough history populates candles and resets the paper balance`() = runTest {
        coinsRemoteDataSource.ohlcResult = Result.Success(FakeCoinsRemoteDataSource.fakeOhlcCandles(20))

        viewModel.selectCoin(testCoin)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(20, state.candles.size)
        assertEquals(0, state.currentCandleIndex)
        assertEquals(10000.0, state.cashBalance)
        assertEquals(10000.0, state.equity)
    }

    @Test
    fun `equity reflects unrealized PnL without dropping by margin the instant a position opens`() = runTest {
        // All-flat candles: price never moves, so a position's unrealized PnL stays 0.
        coinsRemoteDataSource.ohlcResult = Result.Success(FakeCoinsRemoteDataSource.fakeOhlcCandles(10, price = 100.0))
        viewModel.selectCoin(testCoin)

        viewModel.onOrderAmountChanged("1000")
        viewModel.onOrderLeverageChanged("5")
        viewModel.onOrderSideChanged(OrderSideInput.LONG)
        viewModel.openPosition()

        // Margin was deducted from cashBalance the instant the position opened...
        assertEquals(9000.0, viewModel.state.value.cashBalance)

        // ...but equity (cash + locked margin + unrealized PnL) must NOT have
        // dropped along with it, since PnL is still 0 — this is the exact bug
        // fixed by "equity = cashBalance + positions.sumOf { margin + pnl }".
        viewModel.stepForward()
        assertEquals(10000.0, viewModel.state.value.equity)
    }

    @Test
    fun `a long position is force-liquidated and the loss is capped at exactly the margin`() = runTest {
        // Candle 0: entry at 100. Candle 1: low dips to 85, breaching the 10x
        // liquidation price of 90 (entry - entry/leverage) well before it would
        // reach a real 100% price crash — this is what "liquidation" protects
        // against: losses can't run past the trader's posted margin.
        val candles = listOf(
            listOf(0.0, 100.0, 100.0, 100.0, 100.0),
            listOf(1.0, 100.0, 100.0, 85.0, 95.0),
        ) + (2 until 10).map { i -> listOf(i.toDouble(), 95.0, 96.0, 94.0, 95.0) }
        coinsRemoteDataSource.ohlcResult = Result.Success(candles)
        viewModel.selectCoin(testCoin)

        viewModel.onOrderAmountChanged("1000")
        viewModel.onOrderLeverageChanged("10")
        viewModel.onOrderSideChanged(OrderSideInput.LONG)
        viewModel.openPosition()
        assertEquals(9000.0, viewModel.state.value.cashBalance)

        viewModel.stepForward() // advances into candle 1, whose low breaches liquidation

        val state = viewModel.state.value
        assertTrue(state.positions.isEmpty(), "the liquidated position must be closed, not left open")
        assertEquals(1, state.closedTrades.size)
        val trade = state.closedTrades.first()
        assertEquals(ExitReason.LIQUIDATION, trade.exitReason)
        assertEquals(90.0, trade.exitPrice)
        // Loss is exactly -100% of the margin (-1000), never more.
        assertEquals(-1000.0, trade.pnl)
        assertEquals(9000.0, state.cashBalance) // 10000 - margin(1000) + margin back(1000) - loss(1000)
    }
}
