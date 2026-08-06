package dev.kbwallet.app.core.i18n

import platform.Foundation.NSUserDefaults

private const val KEY_LANGUAGE = "app_language"

class IosLanguageStorage : LanguageStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun get(): AppLanguage? =
        AppLanguage.fromCode(defaults.stringForKey(KEY_LANGUAGE))

    override fun save(language: AppLanguage) {
        defaults.setObject(language.code, forKey = KEY_LANGUAGE)
    }
}
