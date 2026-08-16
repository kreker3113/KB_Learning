package dev.kbwallet.app.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

/**
 * Lets a ViewModel's "Retry" button force a real re-fetch on top of a
 * reactive repository [Flow] — `SharingStarted.WhileSubscribed(...)` alone
 * only restarts an upstream flow once all subscribers are gone for its
 * timeout, it doesn't give a way to force a fresh subscription (and
 * therefore a fresh underlying fetch, e.g. through a repository's
 * `flatMapLatest` over a DB flow) on an explicit user tap.
 *
 * Was duplicated near-identically in [dev.kbwallet.app.portfolio.presentation.PortfolioViewModel]
 * and [dev.kbwallet.app.dashboard.presentation.DashboardViewModel]; pulled out here
 * so both share one implementation.
 */
class RetryTrigger {
    private val signal = MutableStateFlow(0)

    /** Re-subscribes every [Flow] built with [retryable] on this trigger. */
    fun retry() {
        signal.update { it + 1 }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> retryable(source: () -> Flow<T>): Flow<T> = signal.flatMapLatest { source() }
}
