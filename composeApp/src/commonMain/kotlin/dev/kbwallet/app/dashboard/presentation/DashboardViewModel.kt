package dev.kbwallet.app.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val getCoinsListUseCase: GetCoinsListUseCase,
) : ViewModel() {

    // isLoading starts true (below) and the collectors in init{} below flip it to
    // false once, permanently, when their first real value arrives — there used
    // to be an onStart { isLoading = true } here too, resetting it back to true
    // on every resubscribe (i.e. every time you left this tab for 5+ seconds and
    // came back, per WhileSubscribed(5000)). Nothing in that resubscribe path
    // ever set it back to false again — the data collectors below only run once,
    // for the ViewModel's whole lifetime — so the spinner got stuck forever the
    // moment you revisited Dashboard after being away. That's what "namertvo
    // zavis" actually was.
    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state: StateFlow<DashboardState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState(isLoading = true)
        )

    // Bumping this forces a fresh subscription to the two reactive flows below
    // (via flatMapLatest) so retry() can re-trigger their underlying network
    // call on demand, not just whenever WhileSubscribed happens to restart them.
    private val retrySignal = MutableStateFlow(0)

    init {
        // ── Portfolio coins (reactive) ──
        viewModelScope.launch {
            portfolioCoinsFlow().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val coins = result.data
                        val totalFiat = coins.sumOf { it.ownedAmountInFiat }
                        val weightedPerf = if (totalFiat > 0) {
                            coins.sumOf { it.performancePercent * it.ownedAmountInFiat } / totalFiat
                        } else 0.0
                        val summaryItems = coins.take(3).map { coin ->
                            DashboardCoinItem(
                                id = coin.coin.id,
                                name = coin.coin.name,
                                symbol = coin.coin.symbol,
                                iconUrl = coin.coin.iconUrl,
                                formattedPrice = formatFiat(coin.ownedAmountInFiat),
                                formattedChange = formatPercentage(coin.performancePercent),
                                isPositive = coin.performancePercent >= 0,
                            )
                        }
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                coinCount = coins.size,
                                portfolioSummaryCoins = summaryItems,
                                recentPerformance = formatPercentage(weightedPerf),
                                isPerformancePositive = weightedPerf >= 0,
                            )
                        }
                    }
                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
                    }
                }
            }
        }

        // ── Total balance (reactive) ──
        viewModelScope.launch {
            totalBalanceFlow().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(portfolioValue = formatFiat(result.data), error = null) }
                    }
                    is Result.Error -> {
                        _state.update { it.copy(error = result.error.toUiText()) }
                    }
                }
            }
        }

        loadTopCoins()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun portfolioCoinsFlow() = retrySignal.flatMapLatest { portfolioRepository.allPortfolioCoinsFlow() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun totalBalanceFlow() = retrySignal.flatMapLatest { portfolioRepository.totalBalanceFlow() }

    // ── Top coins (one-shot) ──
    private fun loadTopCoins() {
        viewModelScope.launch {
            when (val coinsResult = getCoinsListUseCase.execute()) {
                is Result.Success -> {
                    val topCoins = coinsResult.data.take(5).map { coin ->
                        DashboardCoinItem(
                            id = coin.coin.id,
                            name = coin.coin.name,
                            symbol = coin.coin.symbol,
                            iconUrl = coin.coin.iconUrl,
                            formattedPrice = formatFiat(coin.price),
                            formattedChange = formatPercentage(coin.change),
                            isPositive = coin.change >= 0,
                        )
                    }
                    _state.update { it.copy(topCoins = topCoins, error = null) }
                }
                is Result.Error -> {
                    _state.update { it.copy(error = coinsResult.error.toUiText()) }
                }
            }
        }
    }

    /** Re-triggers the portfolio/balance flows and the top-coins fetch after an error. */
    fun retry() {
        retrySignal.update { it + 1 }
        loadTopCoins()
    }
}
