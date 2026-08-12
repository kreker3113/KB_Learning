package dev.kbwallet.app.core.network.auth

import dev.kbwallet.app.core.domain.*
import dev.kbwallet.app.core.domain.user.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import dev.kbwallet.app.core.network.safeCall

class AuthApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:8080" // Android emulator -> host
) {
    suspend fun register(email: String, username: String, password: String): Result<AuthResponse, DataError> {
        return safeCall {
            httpClient.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, username, password))
            }
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse, DataError> {
        return safeCall {
            httpClient.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<AuthResponse, DataError> {
        return safeCall {
            httpClient.post("$baseUrl/api/auth/refresh") {
                headers { append("Authorization", "Bearer $refreshToken") }
            }
        }
    }

    suspend fun getProfile(accessToken: String): Result<User, DataError> {
        return safeCall {
            httpClient.get("$baseUrl/api/user/profile") {
                headers { append("Authorization", "Bearer $accessToken") }
            }
        }
    }
}
