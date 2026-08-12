package dev.kbwallet.app.library.domain

import androidx.compose.runtime.Composable
import dev.kbwallet.app.core.i18n.AppLanguage
import dev.kbwallet.app.core.i18n.LocalAppLanguage

/**
 * Static, bundled reference content. No network/DB layer needed — this is a
 * fixed curriculum shipped with the app.
 *
 * The 17 topics ship in both English ([enLibraryTopics]) and Russian
 * ([ruLibraryTopics]) with matching ids/levels/order, switched by the same
 * [LocalAppLanguage] the rest of the UI chrome reacts to — kept as plain data
 * lists rather than [dev.kbwallet.app.core.i18n.AppStrings] entries since that
 * catalog is for short discrete UI strings, not ~100 paragraphs of content.
 */
object LibraryContent {

    @Composable
    fun topics(): List<LibraryTopic> = when (LocalAppLanguage.current) {
        AppLanguage.RUSSIAN -> ruLibraryTopics
        AppLanguage.ENGLISH -> enLibraryTopics
    }

    @Composable
    fun byId(id: String): LibraryTopic? = topics().find { it.id == id }
}
