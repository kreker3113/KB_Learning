package dev.kbwallet.app.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

/** Wrap the app root with this so [LocalAppLanguage] recomposes on language change. */
@Composable
fun ProvideAppLanguage(content: @Composable () -> Unit) {
    val controller = koinInject<LanguageController>()
    val language by controller.language.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalAppLanguage provides language) {
        content()
    }
}
