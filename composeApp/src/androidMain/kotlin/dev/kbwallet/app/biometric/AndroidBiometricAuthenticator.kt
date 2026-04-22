package dev.kbwallet.app.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dev.kbwallet.app.core.biometric.BiometricAuthNotAvailable
import dev.kbwallet.app.core.biometric.BiometricAuthenticator
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidBiometricAuthenticator(
    private val context: Context,
): BiometricAuthenticator {

    override suspend fun authenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
            throw Exception(BiometricAuthNotAvailable.BIOAUTH_NOT_AVAILABLE.toString())
        }

        return suspendCancellableCoroutine {  continuation ->
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(Exception(errString.toString()))
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }

                    override fun onAuthenticationFailed() {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                }
            )
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate using biometrics")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }
}