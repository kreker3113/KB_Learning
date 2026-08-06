package dev.kbwallet.app.core.i18n

/**
 * Languages the app UI can be displayed in, chosen explicitly by the user in
 * Settings — independent of the OS locale.
 *
 * We don't rely on Compose Multiplatform's built-in resource-locale
 * resolution (composeResources/values-ru) for this: as of Compose
 * Multiplatform 1.7.0 there is no reliable, officially supported way to
 * override it at runtime across platforms (works inconsistently on iOS),
 * see https://youtrack.jetbrains.com/issue/CMP-8376. [[AppStrings]] is our
 * own switchable translation catalog instead.
 */
enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    RUSSIAN("ru");

    companion object {
        fun fromCode(code: String?): AppLanguage? = entries.firstOrNull { it.code == code }

        /** Best-effort default based on the device's system language. */
        fun systemDefault(): AppLanguage = fromCode(systemLanguageCode()) ?: ENGLISH
    }
}

/** Two-letter system language code (e.g. "en", "ru"), platform-specific. */
expect fun systemLanguageCode(): String
