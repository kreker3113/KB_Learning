package dev.kbwallet.app.core.i18n

/** Persists the user's explicit in-app language choice across launches. */
interface LanguageStorage {
    fun get(): AppLanguage?
    fun save(language: AppLanguage)
}
