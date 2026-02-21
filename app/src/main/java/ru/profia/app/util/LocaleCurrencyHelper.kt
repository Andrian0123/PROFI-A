package ru.profia.app.util

import java.util.Locale

/**
 * Определение национальной валюты по региону/локали устройства.
 * Используется для автовыбора валюты в подписке и настройках.
 */
object LocaleCurrencyHelper {

    /** Коды валют, поддерживаемые приложением (RUR, USD, EUR). */
    const val RUR = "RUR"
    const val USD = "USD"
    const val EUR = "EUR"

    private val countryToCurrency = mapOf(
        "RU" to RUR, "BY" to RUR, "KZ" to RUR, "UZ" to RUR, "TJ" to RUR, "KG" to RUR, "AM" to RUR, "AZ" to RUR, "MD" to RUR,
        "US" to USD, "CA" to USD, "GB" to USD,
        "DE" to EUR, "FR" to EUR, "IT" to EUR, "ES" to EUR, "NL" to EUR, "BE" to EUR, "AT" to EUR, "PT" to EUR,
        "RO" to EUR, "PL" to EUR, "HU" to EUR, "CZ" to EUR, "GR" to EUR, "FI" to EUR, "SK" to EUR, "IE" to EUR,
        "BG" to EUR, "HR" to EUR, "LT" to EUR, "LV" to EUR, "EE" to EUR, "SI" to EUR, "CY" to EUR, "LU" to EUR, "MT" to EUR
    )

    /**
     * Возвращает код валюты по локали (страна устройства).
     * Если страна не найдена в маппинге — возвращается RUR по умолчанию.
     */
    fun getCurrencyCodeForLocale(locale: Locale = Locale.getDefault()): String {
        val country = locale.country.takeIf { it.isNotBlank() } ?: "RU"
        return countryToCurrency[country.uppercase()] ?: RUR
    }

    /**
     * Символ валюты для отображения цен (подписка, сметы).
     */
    fun getCurrencySymbol(currencyCode: String): String = when (currencyCode) {
        USD -> "$"
        EUR -> "€"
        else -> "₽" // RUR и прочие
    }

    /** Краткая подпись валюты для блока «Вал/Руб» (яз/ру стиль). */
    fun getCurrencyShortLabel(currencyCode: String): String = when (currencyCode) {
        USD -> "USD"
        EUR -> "EUR"
        else -> "Руб" // RUR
    }

    /**
     * Форматирует сумму с символом выбранной валюты (без конвертации — только отображение).
     */
    fun formatPrice(amount: Number, currencyCode: String): String {
        val symbol = getCurrencySymbol(currencyCode)
        val value = when (amount) {
            is Double -> "%,.0f".format(Locale.US, amount)
            is Int -> "%,d".format(Locale.US, amount)
            else -> amount.toString()
        }
        return when (currencyCode) {
            USD, EUR -> "$symbol$value"
            else -> "$value $symbol"
        }
    }
}
