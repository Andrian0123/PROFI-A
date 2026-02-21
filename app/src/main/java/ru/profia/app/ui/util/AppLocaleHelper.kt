package ru.profia.app.ui.util

import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate

/**
 * Коды языков из блока «Выбор языка» (RU, EN, RO, UZ, DE, TJ, KZ)
 * маппятся в теги локали Android (ru, en, ro, uz, de, tg, kk).
 */
object AppLocaleHelper {
    private val LANGUAGE_TO_LOCALE = mapOf(
        "RU" to "ru",
        "EN" to "en",
        "RO" to "ro",
        "UZ" to "uz",
        "DE" to "de",
        "TJ" to "tg", // таджикский
        "KZ" to "kk"  // казахский
    )

    fun languageToLocaleTag(languageCode: String): String =
        LANGUAGE_TO_LOCALE[languageCode.uppercase()] ?: "ru"

    fun applyLanguage(languageCode: String) {
        val tag = languageToLocaleTag(languageCode)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
