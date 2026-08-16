package dev.kbwallet.app.trade.domain

import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.EmptyResult
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.portfolio.domain.PortfolioCoinModel
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.flow.first

class BuyCoinUseCase(
    private val portfolioRepository: PortfolioRepository,
) {

    suspend fun buyCoin(
        coin: Coin,
        amountInFiat: Double,
        price: Double,
    ): EmptyResult<DataError> {
        val balance = portfolioRepository.cashBalanceFlow().first()
        // The MAX/percentage buy buttons compute amountInFiat as availableBalance *
        // fraction — for fraction == 1.0 that's balance itself, but intermediate
        // unit<->fiat round trips elsewhere in the flow can leave it a few ULPs above
        // the true balance in IEEE-754 double math. Tolerate that instead of bouncing
        // a legitimate "buy with everything I have" as insufficient funds.
        val fiatEpsilon = balance * 1e-9 + 1e-9
        if (balance + fiatEpsilon < amountInFiat) {
            return Result.Error(DataError.Local.INSUFFICIENT_FUNDS)
        }
        val actualAmountInFiat = amountInFiat.coerceAtMost(balance)

        val existingCoinResult = portfolioRepository.getPortfolioCoin(coin.id)
        val existingCoin = when (existingCoinResult) {
            is Result.Success -> existingCoinResult.data
            is Result.Error -> return Result.Error(existingCoinResult.error)
        }
        val amountInUnit = actualAmountInFiat / price
        if (existingCoin != null) {
            val newAmountOwned = existingCoin.ownedAmountInUnit + amountInUnit
            val newTotalInvestment = existingCoin.ownedAmountInFiat + actualAmountInFiat
            val newAveragePurchasePrice = newTotalInvestment / newAmountOwned
            portfolioRepository.savePortfolioCoin(
                existingCoin.copy(
                    ownedAmountInUnit = newAmountOwned,
                    ownedAmountInFiat = newTotalInvestment,
                    averagePurchasePrice = newAveragePurchasePrice
                )
            )
        } else {
            portfolioRepository.savePortfolioCoin(
                PortfolioCoinModel(
                    coin = coin,
                    performancePercent = 0.0,
                    averagePurchasePrice = price,
                    ownedAmountInFiat = actualAmountInFiat,
                    ownedAmountInUnit = amountInUnit
                )
            )
        }
        portfolioRepository.updateCashBalance(balance - actualAmountInFiat)
        portfolioRepository.recordTransaction(
            coinId = coin.id,
            coinName = coin.name,
            coinSymbol = coin.symbol,
            type = "BUY",
            amountInFiat = actualAmountInFiat,
            amountInUnit = amountInUnit,
            pricePerUnit = price,
        )
        return Result.Success(Unit)
    }
}
