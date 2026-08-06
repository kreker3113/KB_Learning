package dev.kbwallet.app.core.i18n

import android.content.Context

private const val PREFS_NAME = "kb_learning_prefs"
private const val KEY_LANGUAGE = "app_language"

class AndroidLanguageStorage(private val context: Context) : LanguageStorage {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun get(): AppLanguage? =
        AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null))

    override fun save(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }
}
