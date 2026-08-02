package dev.kbwallet.app.core.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbwallet.app.core.biometric.BiometricAuthNotAvailable
import dev.kbwallet.app.core.biometric.getBiometricAuthenticator
import dev.kbwallet.app.core.biometric.getPlatformContext
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
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
                text = "KB Learning",
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
                    color = LocalKBLearningColorsPalette.current.lossRed,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                )
            }
        }

        Text(
            text = "This app is for educational purposes only and does not constitute financial advice. Cryptocurrency investments carry risk — do your own research.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        )
    }
}