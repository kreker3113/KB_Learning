package dev.kbwallet.app.biometric

import dev.kbwallet.app.core.biometric.BiometricAuthenticator

/**
 * Desktop has no OS-level biometric API comparable to Android's BiometricPrompt
 * or iOS's LocalAuthentication. Rather than locking desktop users out of the
 * app entirely, treat physical access to the machine as sufficient and pass
 * the gate immediately.
 */
object DesktopBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun authenticate(): Boolean = true
}
