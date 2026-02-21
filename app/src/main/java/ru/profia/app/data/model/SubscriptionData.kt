package ru.profia.app.data.model

enum class SubscriptionType {
    NONE,
    TRIAL,
    MONTHLY,
    HALF_YEAR,
    YEARLY
}

data class SubscriptionData(
    val type: SubscriptionType,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean
)

data class PromoCode(
    val code: String,
    val durationMonths: Int,
    val isUsed: Boolean = false
)
