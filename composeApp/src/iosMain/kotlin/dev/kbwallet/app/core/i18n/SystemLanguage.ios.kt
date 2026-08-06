package dev.kbwallet.app.core.i18n

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun systemLanguageCode(): String = NSLocale.currentLocale.languageCode ?: "en"
