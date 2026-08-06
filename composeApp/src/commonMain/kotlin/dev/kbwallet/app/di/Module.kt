package dev.kbwallet.app.di

import dev.kbwallet.app.analytics.presentation.PnLViewModel
import dev.kbwallet.app.coins.data.remote.impl.KtorCoinsRemoteDataSource
import dev.kbwallet.app.coins.domain.GetCoinDetailsUseCase
import dev.kbwallet.app.coins.domain.GetCoinPriceHistoryUseCase
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.coins.presentation.CoinsListViewModel
import dev.kbwallet.app.core.network.HttpClientFactory
import dev.kbwallet.app.core.network.auth.AuthApiClient
import dev.kbwallet.app.core.security.TokenStorage
import dev.kbwallet.app.core.security.SecureTokenStorage
import dev.kbwallet.app.core.i18n.LanguageController
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import dev.kbwallet.app.portfolio.data.PortfolioRepositoryImpl
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import dev.kbwallet.app.trade.presentation.buy.BuyViewModel
import dev.kbwallet.app.trade.presentation.sell.SellViewModel
import org.koin.dsl.module
import androidx.room.RoomDatabase
import dev.kbwallet.app.core.database.portfolio.PortfolioDatabase
import dev.kbwallet.app.core.database.portfolio.getPortfolioDatabase
import dev.kbwallet.app.simulator.domain.MarketSimulator
import dev.kbwallet.app.dashboard.presentation.DashboardViewModel
import dev.kbwallet.app.history.presentation.HistoryViewModel
import dev.kbwallet.app.portfolio.presentation.PortfolioViewModel
import dev.kbwallet.app.profile.presentation.ProfileViewModel
import dev.kbwallet.app.chart.di.chartModule
import dev.kbwallet.app.trade.domain.BuyCoinUseCase
import dev.kbwallet.app.trade.domain.SellCoinUseCase
import dev.kbwallet.app.simulator.presentation.SimulatorViewModel
import dev.kbwallet.app.watchlist.data.WatchlistRepositoryImpl
import dev.kbwallet.app.watchlist.domain.WatchlistRepository
import dev.kbwallet.app.watchlist.presentation.WatchlistViewModel

fun initKoin(config: KoinAppDeclaration? = null) =
    startKoin {
        config?.invoke(this)
        modules(
            sharedModule,
            platformModule,
        )
    }

expect val platformModule: Module

val sharedModule = module {

    // core
    single<HttpClient> { HttpClientFactory.create(get()) }

    // auth & security
    single { AuthApiClient(get()) }
    single { TokenStorage() }
    single { SecureTokenStorage() }

    // localization
    single { LanguageController(get()) }

    // trade
    singleOf(::BuyCoinUseCase)
    singleOf(::SellCoinUseCase)
    viewModel { (coinId: String) -> BuyViewModel(get(), get(), get(), coinId) }
    viewModel { (coinId: String) -> SellViewModel(get(), get(), get(), coinId) }

    // portfolio
    single {
        getPortfolioDatabase(get<RoomDatabase.Builder<PortfolioDatabase>>())
    }
    singleOf(::PortfolioRepositoryImpl).bind<PortfolioRepository>()
    single { get<PortfolioDatabase>().portfolioDao() }
    single { get<PortfolioDatabase>().userBalanceDao() }
    single { get<PortfolioDatabase>().transactionDao() }
    single { get<PortfolioDatabase>().limitOrderDao() }
    single { get<PortfolioDatabase>().watchlistDao() }
    viewModel { PortfolioViewModel(get()) }

    // coins list
    viewModel { CoinsListViewModel(get(), get()) }
    singleOf(::GetCoinsListUseCase)
    singleOf(::KtorCoinsRemoteDataSource).bind<CoinsRemoteDataSource>()
    singleOf(::GetCoinDetailsUseCase)
    singleOf(::GetCoinPriceHistoryUseCase)

    // dashboard
    viewModel { DashboardViewModel(get(), get()) }

    // history
    viewModel { HistoryViewModel(get()) }

    // profile
    viewModel { ProfileViewModel(get()) }

    // chart
    includes(chartModule)

    // ── Trading Simulator additions ──

    // watchlist
    singleOf(::WatchlistRepositoryImpl).bind<WatchlistRepository>()
    viewModel { WatchlistViewModel(get()) }

    // P&L analytics
    viewModel { PnLViewModel(get()) }

    // market simulator (singleton, shared across app)
    single { MarketSimulator(get(), get(), get(), get()) }

    // trading simulator
    viewModel { SimulatorViewModel(get()) }
}
