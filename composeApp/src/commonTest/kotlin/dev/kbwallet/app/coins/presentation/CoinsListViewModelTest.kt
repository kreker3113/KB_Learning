package dev.kbwallet.app.coins.presentation

import app.cash.turbine.test
import dev.kbwallet.app.coins.data.remote.FakeCoinsRemoteDataSource
import dev.kbwallet.app.coins.domain.GetCoinPriceHistoryUseCase
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.core.domain.DataError
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoinsListViewModelTest {

    private lateinit var viewModel: CoinsListViewModel
    private lateinit var dataSource: FakeCoinsRemoteDataSource
    private lateinit var getCoinsUseCase: GetCoinsListUseCase
    private lateinit var getHistoryUseCase: GetCoinPriceHistoryUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataSource = FakeCoinsRemoteDataSource()
        getCoinsUseCase = GetCoinsListUseCase(dataSource)
        getHistoryUseCase = GetCoinPriceHistoryUseCase(dataSource)
        viewModel = CoinsListViewModel(getCoinsUseCase, getHistoryUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful load maps coins to ui items`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            // Depending on unconfined dispatcher, it might already load data
            if (state.coins.isEmpty() && state.error == null) {
                val nextState = awaitItem()
                assertTrue(nextState.coins.isNotEmpty())
                assertEquals("Bitcoin", nextState.coins[0].name)
            } else {
                assertTrue(state.coins.isNotEmpty())
                assertEquals("Bitcoin", state.coins[0].name)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error during load maps to UiText`() = runTest {
        dataSource.simulateError = true
        viewModel.loadCoins()

        viewModel.state.test {
            val state = awaitItem()
            if (state.error == null) {
                val nextState = awaitItem()
                assertEquals(DataError.Remote.SERVER.toUiText(), nextState.error)
            } else {
                assertEquals(DataError.Remote.SERVER.toUiText(), state.error)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCoinLongPressed loads chart data`() = runTest {
        // We first need the coins to be loaded
        viewModel.onCoinLongPressed("1")

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.chartState)
            assertFalse(state.chartState!!.isLoading)
            // Note: FakeCoinsRemoteDataSource returns empty lists for history by default
            cancelAndIgnoreRemainingEvents()
        }
    }
}
