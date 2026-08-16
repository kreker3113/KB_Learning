package dev.kbwallet.app.trade.domain

import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.EmptyResult
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.flow.first

class SellCoinUseCase(
    private val portfolioRepository: PortfolioRepository,
) {
    suspend fun sellCoin(
        coin: Coin,
        amountInFiat: Double,
        price: Double,
    ): EmptyResult<DataError> {
        val sellAllThreshold = 1
        when (val existingCoinResponse = portfolioRepository.getPortfolioCoin(coin.id)) {
            is Result.Success -> {
                val existingCoin = existingCoinResponse.data
                    ?: return Result.Error(DataError.Local.INSUFFICIENT_FUNDS)
                val requestedSellAmountInUnit = amountInFiat / price
                // The MAX/percentage sell buttons compute amountInFiat as a unit->fiat
                // conversion of everything owned — converting it back to units here can
                // drift a few ULPs above ownedAmountInUnit in IEEE-754 double math.
                // Tolerate that (proportionally, so it scales for both large holdings
                // and tiny fractional ones) instead of bouncing a legitimate sell-all.
                val unitEpsilon = existingCoin.ownedAmountInUnit * 1e-9 + 1e-12
                if (existingCoin.ownedAmountInUnit + unitEpsilon < requestedSellAmountInUnit) {
                    return Result.Error(DataError.Local.INSUFFICIENT_FUNDS)
                }
                val sellAmountInUnit = requestedSellAmountInUnit.coerceAtMost(existingCoin.ownedAmountInUnit)
                val actualAmountInFiat = sellAmountInUnit * price
                val balance = portfolioRepository.cashBalanceFlow().first()
                val remainingAmountFiat = existingCoin.ownedAmountInFiat - actualAmountInFiat
                val remainingAmountUnit = existingCoin.ownedAmountInUnit - sellAmountInUnit
                if (remainingAmountFiat < sellAllThreshold) {
                    portfolioRepository.removeCoinFromPortfolio(coin.id)
                } else {
                    portfolioRepository.savePortfolioCoin(
                        existingCoin.copy(
                            ownedAmountInUnit = remainingAmountUnit,
                            ownedAmountInFiat = remainingAmountFiat,
                        )
                    )
                }
                portfolioRepository.updateCashBalance(balance + actualAmountInFiat)
                portfolioRepository.recordTransaction(
                    coinId = coin.id,
                    coinName = coin.name,
                    coinSymbol = coin.symbol,
                    type = "SELL",
                    amountInFiat = actualAmountInFiat,
                    amountInUnit = sellAmountInUnit,
                    pricePerUnit = price,
                )
                return Result.Success(Unit)
            }
            is Result.Error -> {
                return existingCoinResponse
            }
        }
    }
}
