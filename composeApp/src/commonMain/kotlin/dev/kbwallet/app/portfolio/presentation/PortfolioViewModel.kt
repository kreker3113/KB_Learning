package dev.kbwallet.app.portfolio.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.formatCoinUnit
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.portfolio.domain.PortfolioCoinModel
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PortfolioViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState(isLoading = true))

    // Bumping this forces a fresh subscription to the repository's flows below
    // (via flatMapLatest), which is what actually re-runs their underlying
    // network call — WhileSubscribed(5000) alone only restarts them if the
    // screen was left for 5+ seconds, not on an explicit user-triggered
    // "Retry" tap. See retry().
    private val retrySignal = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val portfolioCoinsFlow = retrySignal.flatMapLatest { portfolioRepository.allPortfolioCoinsFlow() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val totalBalanceFlowRetryable = retrySignal.flatMapLatest { portfolioRepository.totalBalanceFlow() }

    val state: StateFlow<PortfolioState> = combine(
        _state,
        portfolioCoinsFlow,
        totalBalanceFlowRetryable,
        portfolioRepository.cashBalanceFlow(),
    ) { currentState, portfolioCoinsResponse, totalBalanceResult, cashBalance ->
        when (portfolioCoinsResponse) {
            is Result.Success -> {
                handleSuccessState(
                    currentState = currentState,
                    portfolioCoins = portfolioCoinsResponse.data,
                    totalBalanceResult = totalBalanceResult,
                    cashBalance = cashBalance
                )
            }
            is Result.Error -> {
                handleErrorState(
                    currentState = currentState,
                    portfolioCoinsResponse.error
                )
            }
        }
    }.onStart {
        portfolioRepository.initializeBalance()
    }.flowOn(coroutineDispatcher).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PortfolioState(isLoading = true)
    )

    /** Re-triggers both the portfolio-coins and total-balance fetches after an error. */
    fun retry() {
        retrySignal.update { it + 1 }
    }

    private fun handleSuccessState(
        currentState: PortfolioState,
        portfolioCoins: List<PortfolioCoinModel>,
        totalBalanceResult: Result<Double, DataError>,
        cashBalance: Double
    ): PortfolioState {
        // The coin list itself loaded fine, so it's still shown — only the total
        // balance figure and the error banner reflect this failure, rather than
        // silently showing "$0" as if the user's balance really were zero.
        val portfolioValue = when (totalBalanceResult) {
            is Result.Success -> formatFiat(totalBalanceResult.data)
            is Result.Error -> currentState.portfolioValue
        }

        return currentState.copy(
            coins = portfolioCoins.map { it.toUiPortfolioCoinItem() },
            portfolioValue = portfolioValue,
            cashBalance = formatFiat(cashBalance),
            showBuyButton = portfolioCoins.isNotEmpty(),
            isLoading = false,
            error = (totalBalanceResult as? Result.Error)?.error?.toUiText(),
        )
    }

    private fun handleErrorState(
        currentState: PortfolioState,
        error: DataError,
    ): PortfolioState {
        return currentState.copy(
            isLoading = false,
            error = error.toUiText()
        )
    }

    private fun PortfolioCoinModel.toUiPortfolioCoinItem(): UiPortfolioCoinItem {
        return UiPortfolioCoinItem(
            id = coin.id,
            name = coin.name,
            iconUrl = coin.iconUrl,
            amountInUnitText = formatCoinUnit(ownedAmountInUnit, coin.symbol),
            amountInFiatText = formatFiat(ownedAmountInFiat),
            performancePercentText = formatPercentage(performancePercent),
            isPositive = performancePercent >= 0,
            symbol = coin.symbol,
            amountInFiat = ownedAmountInFiat,
        )
    }
}
