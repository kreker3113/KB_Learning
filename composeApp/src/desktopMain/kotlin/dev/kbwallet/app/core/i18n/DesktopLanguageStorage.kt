package dev.kbwallet.app.core.i18n

import java.util.prefs.Preferences

private const val KEY_LANGUAGE = "app_language"

class DesktopLanguageStorage : LanguageStorage {
    private val prefs = Preferences.userRoot().node("dev/kbwallet/app")

    override fun get(): AppLanguage? =
        AppLanguage.fromCode(prefs.get(KEY_LANGUAGE, null))

    override fun save(language: AppLanguage) {
        prefs.put(KEY_LANGUAGE, language.code)
    }
}
