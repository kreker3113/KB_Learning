package dev.kbwallet.app.core.biometric

import androidx.compose.runtime.Composable

interface PlatformContext
@Composable
expect fun getPlatformContext(): PlatformContext

interface BiometricAuthenticator {
    suspend fun authenticate(): Boolean
}

expect fun getBiometricAuthenticator(context: PlatformContext): BiometricAuthenticator

enum class BiometricAuthNotAvailable {
    BIOAUTH_NOT_AVAILABLE
}