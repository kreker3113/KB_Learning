package dev.kbwallet.app.dashboard.presentation

import app.cash.turbine.test
import dev.kbwallet.app.coins.data.remote.FakeCoinsRemoteDataSource
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.core.domain.DataError
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var portfolioRepository: FakePortfolioRepository
    private lateinit var coinsDataSource: FakeCoinsRemoteDataSource
    private lateinit var getCoinsListUseCase: GetCoinsListUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        portfolioRepository = FakePortfolioRepository()
        coinsDataSource = FakeCoinsRemoteDataSource()
        getCoinsListUseCase = GetCoinsListUseCase(coinsDataSource)
        viewModel = DashboardViewModel(portfolioRepository, getCoinsListUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful load populates portfolio summary and top coins`() = runTest {
        portfolioRepository.savePortfolioCoin(FakePortfolioRepository.portfolioCoin)

        viewModel.state.test {
            val state = awaitItem()
            // Should eventually load data
            if (state.isLoading) {
                val loadedState = awaitItem()
                assertFalse(loadedState.isLoading)
                assertTrue(loadedState.topCoins.isNotEmpty())
                assertTrue(loadedState.portfolioSummaryCoins.isNotEmpty())
            } else {
                assertTrue(state.topCoins.isNotEmpty())
                assertTrue(state.portfolioSummaryCoins.isNotEmpty())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error mapping propagates to ui state`() = runTest {
        coinsDataSource.simulateError = true
        viewModel.loadData()

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
}
