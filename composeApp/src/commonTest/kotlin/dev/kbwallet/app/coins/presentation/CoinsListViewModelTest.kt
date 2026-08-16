package dev.kbwallet.app.coins.presentation

import app.cash.turbine.test
import dev.kbwallet.app.coins.data.FakeCoinsRemoteDataSource
import dev.kbwallet.app.coins.domain.GetCoinPriceHistoryUseCase
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.coins.data.remote.dto.MarketChartDto
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoinsListViewModelTest {

    private lateinit var coinsRemoteDataSource: FakeCoinsRemoteDataSource
    private lateinit var viewModel: CoinsListViewModel

    @BeforeTest
    fun setup() {
        // CoinsListViewModel has no injectable dispatcher — it launches directly
        // on viewModelScope, which resolves Dispatchers.Main.immediate.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coinsRemoteDataSource = FakeCoinsRemoteDataSource()
        viewModel = CoinsListViewModel(
            getCoinsListUseCase = GetCoinsListUseCase(coinsRemoteDataSource),
            getCoinPriceHistoryUseCase = GetCoinPriceHistoryUseCase(coinsRemoteDataSource),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates the coin list`() = runTest {
        coinsRemoteDataSource.coinsResult = Result.Success(listOf(FakeCoinsRemoteDataSource.fakeCoinMarketDto))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.coins.size)
            assertEquals("bitcoin", state.coins.first().id)
        }
    }

    @Test
    fun `initial load failure surfaces the error and clears the coin list`() = runTest {
        coinsRemoteDataSource.coinsResult = Result.Error(DataError.Remote.NO_INTERNET)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.coins.isEmpty())
            assertEquals(DataError.Remote.NO_INTERNET.toUiText(), state.error)
        }
    }

    @Test
    fun `retry after a failure recovers the coin list`() = runTest {
        coinsRemoteDataSource.coinsResult = Result.Error(DataError.Remote.SERVER)

        viewModel.state.test {
            assertTrue(awaitItem().coins.isEmpty())

            coinsRemoteDataSource.coinsResult = Result.Success(listOf(FakeCoinsRemoteDataSource.fakeCoinMarketDto))
            viewModel.retry()

            val state = awaitItem()
            assertEquals(1, state.coins.size)
            assertEquals(null, state.error)
        }
    }

    @Test
    fun `long-pressing a coin loads its sparkline`() = runTest {
        coinsRemoteDataSource.coinsResult = Result.Success(listOf(FakeCoinsRemoteDataSource.fakeCoinMarketDto))
        coinsRemoteDataSource.priceHistoryResult = Result.Success(
            MarketChartDto(prices = listOf(listOf(1000.0, 10.0), listOf(2000.0, 12.0)))
        )

        viewModel.state.test {
            awaitItem() // initial coin list

            viewModel.onCoinLongPressed("bitcoin")
            val loading = awaitItem()
            assertTrue(loading.chartState?.isLoading == true)

            val loaded = awaitItem()
            assertEquals(listOf(10.0, 12.0), loaded.chartState?.sparkLine)
            assertEquals(false, loaded.chartState?.isLoading)
        }
    }
}
