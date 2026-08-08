package dev.kbwallet.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.kbwallet.app.di.initKoin

fun main() {
    initKoin()
    application {
        val windowState = rememberWindowState(
            width = 1200.dp,
            height = 840.dp,
            position = WindowPosition(alignment = Alignment.Center),
        )
        Window(
            onCloseRequest = ::exitApplication,
            title = "KB Wallet",
            state = windowState,
        ) {
            App()
        }
    }
}
