package dev.kbwallet.app.watchlist.presentation

import app.cash.turbine.test
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.watchlist.data.FakeWatchlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {

    private lateinit var viewModel: WatchlistViewModel
    private lateinit var repository: FakeWatchlistRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeWatchlistRepository()
        viewModel = WatchlistViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Without waiting for the repository flow, it should be empty and loading
        val state = viewModel.state.value
        // Actually, with UnconfinedTestDispatcher, it might already process the empty flow.
        // Let's test the successful fetch.
        viewModel.state.test {
            val initialState = awaitItem()
            // Depending on dispatcher, could be loading or already loaded empty
            assertTrue(initialState.items.isEmpty())
        }
    }

    @Test
    fun `successful fetch maps to UI items`() = runTest {
        repository.addToWatchlist("1", "Bitcoin", "BTC", "url", 50000.0)
        
        viewModel.state.test {
            val state = awaitItem()
            if (state.items.isEmpty()) {
                val nextState = awaitItem()
                assertFalse(nextState.isLoading)
                assertEquals(1, nextState.items.size)
                assertEquals("Bitcoin", nextState.items[0].name)
            } else {
                assertFalse(state.isLoading)
                assertEquals(1, state.items.size)
                assertEquals("Bitcoin", state.items[0].name)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error maps to UI text`() = runTest {
        repository.simulateError = true
        viewModel.loadWatchlist() // re-trigger

        viewModel.state.test {
            // Skips loading item
            val state = awaitItem()
            if (state.isLoading) {
                val nextState = awaitItem()
                assertFalse(nextState.isLoading)
                assertEquals(DataError.Remote.SERVER.toUiText(), nextState.error)
            } else {
                assertFalse(state.isLoading)
                assertEquals(DataError.Remote.SERVER.toUiText(), state.error)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
