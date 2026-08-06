package dev.kbwallet.app.core.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the currently selected [AppLanguage] and persists changes to it.
 * Singleton (Koin), observed app-wide via [ProvideAppLanguage]/[LocalAppLanguage].
 */
class LanguageController(private val storage: LanguageStorage) {

    private val _language = MutableStateFlow(storage.get() ?: AppLanguage.systemDefault())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _language.value = language
        storage.save(language)
    }
}
