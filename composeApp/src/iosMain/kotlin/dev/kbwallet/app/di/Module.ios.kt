package dev.kbwallet.app.di

import androidx.room.RoomDatabase
import dev.kbwallet.app.core.database.getPortfolioDatabaseBuilder
import dev.kbwallet.app.core.database.portfolio.PortfolioDatabase
import dev.kbwallet.app.core.i18n.IosLanguageStorage
import dev.kbwallet.app.core.i18n.LanguageStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    singleOf(::getPortfolioDatabaseBuilder).bind<RoomDatabase.Builder<PortfolioDatabase>>()
    single<LanguageStorage> { IosLanguageStorage() }
}