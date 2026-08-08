package dev.kbwallet.app.core.biometric

import androidx.compose.runtime.Composable
import dev.kbwallet.app.biometric.DesktopBiometricAuthenticator

object DesktopPlatformContext : PlatformContext

@Composable
actual fun getPlatformContext(): PlatformContext = DesktopPlatformContext

actual fun getBiometricAuthenticator(context: PlatformContext): BiometricAuthenticator =
    DesktopBiometricAuthenticator
