package dev.kbwallet.app.core.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.core.biometric.BiometricAuthNotAvailable
import dev.kbwallet.app.core.biometric.getBiometricAuthenticator
import dev.kbwallet.app.core.biometric.getPlatformContext
import dev.kbwallet.app.theme.LocalKBWalletColorsPalette
import kotlinx.coroutines.launch

@Composable
fun BiometricScreen(
    onSuccess: () -> Unit,
) {
    val platformContext = getPlatformContext()
    val biometricAuthenticator = remember { getBiometricAuthenticator(platformContext) }
    val coroutineScope = rememberCoroutineScope()
    var authError by remember { mutableStateOf<String?>(null) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "KBWallet",
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Powered by Compose Multiplatform",
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(64.dp))
            // TODO Icon
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val authenticated = biometricAuthenticator.authenticate()
                            authError = null
                            if (authenticated) {
                                onSuccess()
                            }
                        } catch (e: Exception) {
                            authError = e.message
                            if (e.message == BiometricAuthNotAvailable.BIOAUTH_NOT_AVAILABLE.toString()) {
                                authError = "Biometric is not available on your device!"
                            }
                        }
                    }
                }
            ) {
                Text(
                    text = "Login"
                )
            }
            authError?.let {
                Text(
                    text = it,
                    color = LocalKBWalletColorsPalette.current.lossRed,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                )
            }
        }
    }
}