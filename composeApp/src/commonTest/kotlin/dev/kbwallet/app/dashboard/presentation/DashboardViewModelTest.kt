package dev.kbwallet.app.dashboard.presentation

import app.cash.turbine.test
import dev.kbwallet.app.coins.data.FakeCoinsRemoteDataSource
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.portfolio.data.FakePortfolioRepository
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

/**
 * Regression coverage for a code-review finding on the error-retry-ui PR:
 * DashboardState.error used to be written by three independent async
 * sources (portfolio coins, total balance, top coins) that each blindly set
 * `error = null` on their own success — an unrelated source recovering
 * could silently hide another source's still-active error. See
 * DashboardViewModel.combinedError()/per-source error fields.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var portfolioRepository: FakePortfolioRepository
    private lateinit var coinsRemoteDataSource: FakeCoinsRemoteDataSource

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        portfolioRepository = FakePortfolioRepository()
        coinsRemoteDataSource = FakeCoinsRemoteDataSource()
        coinsRemoteDataSource.coinsResult = Result.Success(listOf(FakeCoinsRemoteDataSource.fakeCoinMarketDto))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = DashboardViewModel(
        portfolioRepository = portfolioRepository,
        getCoinsListUseCase = GetCoinsListUseCase(coinsRemoteDataSource),
    )

    @Test
    fun `initial load combines portfolio, balance and top coins with no error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.coinCount)
            assertEquals(formatFiat(10000.0), state.portfolioValue)
            assertEquals(1, state.topCoins.size)
            assertNull(state.error)
        }
    }

    @Test
    fun `a portfolio-coins failure surfaces as the dashboard error`() = runTest {
        portfolioRepository.simulateError()
        val viewModel = buildViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(DataError.Remote.SERVER.toUiText(), state.error)
        }
    }

    @Test
    fun `an unrelated source recovering does not clear a still-active error from another source`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem() // initial, healthy state — establishes the subscription

            portfolioRepository.simulateTotalBalanceError(DataError.Remote.NO_INTERNET)
            assertEquals(DataError.Remote.NO_INTERNET.toUiText(), viewModel.state.value.error)

            // An unrelated source (portfolio coins) succeeding must NOT wipe out
            // the still-active total-balance error.
            portfolioRepository.savePortfolioCoin(FakePortfolioRepository.portfolioCoin)
            assertEquals(DataError.Remote.NO_INTERNET.toUiText(), viewModel.state.value.error)
            assertEquals(1, viewModel.state.value.coinCount) // the unrelated success did still apply

            // Once the actual failing source recovers, the error clears.
            portfolioRepository.clearTotalBalanceError()
            portfolioRepository.updateCashBalance(10000.0) // re-trigger totalBalanceFlow's combine
            assertNull(viewModel.state.value.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
