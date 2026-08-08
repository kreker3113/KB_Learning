package dev.kbwallet.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.kbwallet.app.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KB Wallet",
            state = rememberWindowState(width = 420.dp, height = 900.dp),
        ) {
            App()
        }
    }
}
