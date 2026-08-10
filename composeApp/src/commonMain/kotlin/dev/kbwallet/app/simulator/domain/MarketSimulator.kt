package dev.kbwallet.app.simulator.domain

import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.history.data.LimitOrderEntity
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import dev.kbwallet.app.trade.domain.BuyCoinUseCase
import dev.kbwallet.app.trade.domain.SellCoinUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Фоновый симулятор рынка.
 * Периодически обновляет цены монет, исполняет лимитные ордера.
 */
class MarketSimulator(
    private val coinsRemoteDataSource: CoinsRemoteDataSource,
    private val portfolioRepository: PortfolioRepository,
    private val buyCoinUseCase: BuyCoinUseCase,
    private val sellCoinUseCase: SellCoinUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    private var isRunning = false

    fun start(intervalMs: Long = 30000L) {
        if (isRunning) return
        isRunning = true
        scope.launch {
            while (isActive && isRunning) {
                checkAndExecuteOrders()
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        isRunning = false
    }

    /**
     * Проверяет активные лимитные ордера и исполняет их при достижении цены.
     */
    private suspend fun checkAndExecuteOrders() {
        val activeOrders = portfolioRepository.getActiveLimitOrdersList()
        if (activeOrders.isEmpty()) return

        // Get current prices
        val coinsResult = coinsRemoteDataSource.getListOfCoins()
        if (coinsResult !is Result.Success) return
        val coinsMap = coinsResult.data.associateBy { it.id }

        for (order in activeOrders) {
            val currentCoin = coinsMap[order.coinId] ?: continue
            val currentPrice = currentCoin.currentPrice
            val shouldExecute = when (order.type) {
                "LIMIT" -> {
                    if (order.side == "BUY") currentPrice <= order.targetPrice
                    else currentPrice >= order.targetPrice
                }
                "STOP_LOSS" -> {
                    if (order.side == "SELL") currentPrice <= order.targetPrice
                    else false
                }
                "TAKE_PROFIT" -> {
                    if (order.side == "SELL") currentPrice >= order.targetPrice
                    else false
                }
                else -> false
            }

            if (shouldExecute) {
                executeOrder(order, currentPrice)
            }
        }
    }

    private suspend fun executeOrder(order: LimitOrderEntity, price: Double) {
        val coin = dev.kbwallet.app.core.domain.coin.Coin(
            id = order.coinId,
            name = order.coinName,
            symbol = order.coinSymbol,
            iconUrl = order.iconUrl,
        )
        if (order.side == "BUY") {
            buyCoinUseCase.buyCoin(coin, order.amountInFiat, price)
        } else {
            sellCoinUseCase.sellCoin(coin, order.amountInFiat, price)
        }
        portfolioRepository.cancelLimitOrder(order.id) // mark as FILLED
    }

    /**
     * Симуляция изменения цен (для офлайн-тренировки).
     * Применяет случайный сдвиг к ценам из API.
     */
    fun simulatePriceMovement(price: Double, volatility: Double = 0.02): Double {
        val change = (Random.nextDouble() - 0.45) * volatility * price // slight upward bias
        return (price + change).coerceAtLeast(0.0001)
    }
}
