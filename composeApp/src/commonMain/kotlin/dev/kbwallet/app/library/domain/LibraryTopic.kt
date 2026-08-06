package dev.kbwallet.app.library.domain

import dev.kbwallet.app.core.i18n.AppStrings

enum class LibraryLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

fun LibraryLevel.label(strings: AppStrings): String = when (this) {
    LibraryLevel.BEGINNER -> strings.libraryLevelBeginner
    LibraryLevel.INTERMEDIATE -> strings.libraryLevelIntermediate
    LibraryLevel.ADVANCED -> strings.libraryLevelAdvanced
}

data class LibraryTopic(
    val id: String,
    val title: String,
    val level: LibraryLevel,
    val summary: String,
    val content: List<String>,
)
