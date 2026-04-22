package dev.kbwallet.app.core.biometric

import androidx.compose.runtime.Composable
import dev.kbwallet.app.biometric.IosBiometricAuthenticator

object IosPlatformContext : PlatformContext

@Composable
actual fun getPlatformContext(): PlatformContext = IosPlatformContext

actual fun getBiometricAuthenticator(context: PlatformContext): BiometricAuthenticator =
    IosBiometricAuthenticator()