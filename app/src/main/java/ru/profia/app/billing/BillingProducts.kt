package ru.profia.app.billing

/**
 * Product ID подписок в Google Play Console.
 * Создайте в Play Console → Monetization → Products → Subscriptions.
 */
object BillingProducts {
    const val SUB_1_MONTH = "profi_a_sub_1"
    const val SUB_6_MONTHS = "profi_a_sub_6"
    const val SUB_12_MONTHS = "profi_a_sub_12"

    fun productIdForMonths(months: Int): String = when (months) {
        1 -> SUB_1_MONTH
        6 -> SUB_6_MONTHS
        12 -> SUB_12_MONTHS
        else -> SUB_1_MONTH
    }
}
