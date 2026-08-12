package dev.kbwallet.app.watchlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.watchlist.domain.WatchlistItem
import dev.kbwallet.app.watchlist.domain.WatchlistRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WatchlistState(
    val items: List<UiWatchlistItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: org.jetbrains.compose.resources.StringResource? = null,
)

data class UiWatchlistItem(
    val id: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val formattedPrice: String,
    val formattedChange: String,
    val isPositive: Boolean,
    val addedPriceFormatted: String,
)

class WatchlistViewModel(
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistState())
    val state: StateFlow<WatchlistState> = _state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WatchlistState())

    private var job: Job? = null

    init {
        loadWatchlist()
    }

    fun loadWatchlist() {
        job?.cancel()
        _state.value = _state.value.copy(isLoading = true, error = null)
        job = viewModelScope.launch {
            watchlistRepository.getWatchlistWithPrices().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.value = WatchlistState(
                            isLoading = false,
                            items = result.data.map { it.toUi() },
                            error = null
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.error.toUiText()
                        )
                    }
                }
            }
        }
    }

    fun removeItem(coinId: String) {
        viewModelScope.launch {
            watchlistRepository.removeFromWatchlist(coinId)
        }
    }
}

private fun WatchlistItem.toUi(): UiWatchlistItem = UiWatchlistItem(
    id = coin.id,
    name = coin.name,
    symbol = coin.symbol,
    iconUrl = coin.iconUrl,
    formattedPrice = formatFiat(currentPrice),
    formattedChange = formatPercentage(change24h),
    isPositive = change24h >= 0,
    addedPriceFormatted = formatFiat(addedPrice),
)
