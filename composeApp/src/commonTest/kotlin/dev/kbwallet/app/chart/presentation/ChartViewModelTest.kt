package dev.kbwallet.app.chart.presentation

import dev.kbwallet.app.chart.data.FakeKlineDataSource
import dev.kbwallet.app.chart.domain.GetChartDataUseCase
import dev.kbwallet.app.chart.domain.model.TimeRange
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Also covers the "stale chart left on screen after a failed range switch"
 * bug found in code review on the error-retry-ui PR: loadData() must clear
 * candles before a new fetch starts, not just isLoading/error.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModelTest {

    private lateinit var klineDataSource: FakeKlineDataSource
    private lateinit var viewModel: ChartViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        klineDataSource = FakeKlineDataSource()
        viewModel = ChartViewModel(GetChartDataUseCase(klineDataSource))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads candles and computes price change`() = runTest {
        klineDataSource.result = Result.Success(FakeKlineDataSource.fakeCandles(5, startPrice = 100.0))

        viewModel.init("bitcoin", "Bitcoin")

        val state = viewModel.state.value
        assertEquals(5, state.candles.size)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        // Last candle's close (104) minus first candle's close (100) = +4
        assertEquals(4.0, state.priceChange)
    }

    @Test
    fun `init failure surfaces the error and leaves no candles`() = runTest {
        klineDataSource.result = Result.Error(DataError.Remote.SERVER)

        viewModel.init("bitcoin", "Bitcoin")

        val state = viewModel.state.value
        assertTrue(state.candles.isEmpty())
        assertEquals(DataError.Remote.SERVER.toUiText(), state.error)
    }

    @Test
    fun `switching time range clears the previous range's candles even on failure`() = runTest {
        klineDataSource.result = Result.Success(FakeKlineDataSource.fakeCandles(5))
        viewModel.init("bitcoin", "Bitcoin")
        assertTrue(viewModel.state.value.candles.isNotEmpty())

        klineDataSource.result = Result.Error(DataError.Remote.SERVER)
        viewModel.selectTimeRange(TimeRange.ONE_WEEK)

        val state = viewModel.state.value
        assertTrue(state.candles.isEmpty(), "stale candles from the previous range must not remain on screen")
        assertEquals(DataError.Remote.SERVER.toUiText(), state.error)
    }

    @Test
    fun `retry re-fetches the current range after a failure`() = runTest {
        klineDataSource.result = Result.Error(DataError.Remote.SERVER)
        viewModel.init("bitcoin", "Bitcoin")
        assertTrue(viewModel.state.value.error != null)

        klineDataSource.result = Result.Success(FakeKlineDataSource.fakeCandles(5))
        viewModel.retry()

        val state = viewModel.state.value
        assertEquals(5, state.candles.size)
        assertNull(state.error)
    }
}
