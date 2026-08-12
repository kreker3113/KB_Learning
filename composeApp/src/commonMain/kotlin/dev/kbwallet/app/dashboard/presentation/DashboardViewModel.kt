package dev.kbwallet.app.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.kbwallet.app.core.util.toUiText

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

    private var portfolioCoinsJob: kotlinx.coroutines.Job? = null
    private var balanceJob: kotlinx.coroutines.Job? = null
    private var topCoinsJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        portfolioCoinsJob?.cancel()
        balanceJob?.cancel()
        topCoinsJob?.cancel()

        // ── Portfolio coins (reactive) ──
        portfolioCoinsJob = viewModelScope.launch {
            portfolioRepository.allPortfolioCoinsFlow().collect { result ->
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
                                coinCount = coins.size,
                                portfolioSummaryCoins = summaryItems,
                                recentPerformance = formatPercentage(weightedPerf),
                                isPerformancePositive = weightedPerf >= 0,
                            )
                        }
                    }
                    is dev.kbwallet.app.core.domain.Result.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
                    }
                }
            }
        }

        // ── Total balance (reactive) ──
        balanceJob = viewModelScope.launch {
            portfolioRepository.totalBalanceFlow().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(portfolioValue = formatFiat(result.data)) }
                    }
                    is dev.kbwallet.app.core.domain.Result.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
                    }
                }
            }
        }

        // ── Top coins (one-shot) ──
        topCoinsJob = viewModelScope.launch {
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
                    _state.update { it.copy(topCoins = topCoins, isLoading = false) }
                }
                is dev.kbwallet.app.core.domain.Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = coinsResult.error.toUiText()) }
                }
            }
        }
    }
}
