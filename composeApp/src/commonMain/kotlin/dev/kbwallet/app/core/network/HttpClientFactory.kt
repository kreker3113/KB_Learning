package dev.kbwallet.app.core.network


import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        // CoinRanking quotes its numeric fields as JSON strings
                        // (price, change, etc. — presumably to avoid float
                        // precision loss), which strict kotlinx.serialization
                        // rejects for Double/Int properties by default. Without
                        // this every coin/price-history response silently threw
                        // a SerializationException inside safeCall's catch block
                        // and every coin-dependent screen (list, chart,
                        // simulator, buy/sell) rendered permanently empty with
                        // no visible error — this was masked for a long time
                        // because the shared demo key was itself dead/rate-
                        // limited, so a real 200 response never reached the
                        // deserializer until now.
                        isLenient = true
                    }
                )
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }
            install(HttpCache)
            defaultRequest {
                headers { append("x-cg-demo-api-key", ApiKeys.COIN_GECKO) }
                contentType(ContentType.Application.Json)
            }
        }
    }
}