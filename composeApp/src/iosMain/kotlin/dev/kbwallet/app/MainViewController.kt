package dev.kbwallet.app

import androidx.compose.ui.window.ComposeUIViewController
import dev.kbwallet.app.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }