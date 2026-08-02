package dev.kbwallet.app.library.domain

enum class LibraryLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
}

data class LibraryTopic(
    val id: String,
    val title: String,
    val level: LibraryLevel,
    val summary: String,
    val content: List<String>,
)
