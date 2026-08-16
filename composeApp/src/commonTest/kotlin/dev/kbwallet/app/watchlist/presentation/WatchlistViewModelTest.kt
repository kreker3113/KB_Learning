package dev.kbwallet.app.watchlist.presentation

import app.cash.turbine.test
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.watchlist.data.FakeWatchlistRepository
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
class WatchlistViewModelTest {

    private lateinit var repository: FakeWatchlistRepository
    private lateinit var viewModel: WatchlistViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeWatchlistRepository()
        viewModel = WatchlistViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty watchlist loads with an empty item list`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.items.isEmpty())
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `watchlist items are mapped and formatted for display`() = runTest {
        repository.seed(FakeWatchlistRepository.fakeItem)

        viewModel.state.test {
            val state = awaitItem()
            val item = state.items.single()
            assertEquals("bitcoin", item.id)
            assertEquals(formatFiat(65000.0), item.formattedPrice)
            assertEquals(formatPercentage(2.5), item.formattedChange)
            assertEquals(true, item.isPositive)
            assertEquals(formatFiat(60000.0), item.addedPriceFormatted)
        }
    }

    @Test
    fun `removing an item updates the list`() = runTest {
        repository.seed(FakeWatchlistRepository.fakeItem)

        viewModel.state.test {
            assertEquals(1, awaitItem().items.size)

            viewModel.removeItem("bitcoin")

            assertTrue(awaitItem().items.isEmpty())
        }
    }
}
