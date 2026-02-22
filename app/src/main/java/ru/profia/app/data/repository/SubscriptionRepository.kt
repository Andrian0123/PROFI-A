package ru.profia.app.data.repository

import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.profia.app.data.local.datastore.PreferencesDataStore
import ru.profia.app.data.model.SubscriptionData
import ru.profia.app.data.model.SubscriptionType
import ru.profia.app.data.remote.PurchaseVerificationApi
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Режим подписки и авторизация («Начать» / «Авторизоваться») обязательны с 01.06.2026. До этой даты — полный доступ без подписки. */
private val SUBSCRIPTION_REQUIRED_FROM_MILLIS: Long = run {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, 2026)
    cal.set(Calendar.MONTH, Calendar.JUNE)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.timeInMillis
}

private val PROMO_CODES = mapOf(
    "PROMO3" to 3,
    "PROMO6" to 6,
    "PROMO12" to 12
)

@Singleton
class SubscriptionRepository @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val purchaseVerificationApi: PurchaseVerificationApi,
    @Named("scanner_requires_subscription") private val scannerRequiresSubscription: Boolean
) {
    val subscription: Flow<SubscriptionData> = preferencesDataStore.subscriptionFlow

    /**
     * Доступ к сканеру входит в подписку.
     * До даты активации подписки (01.06.2026) — доступ у всех, расходы на сканер несём мы.
     * После этой даты: сборка по ссылке (SCANNER_REQUIRES_SUBSCRIPTION=false) — сканер для теста, доступ у всех;
     * сборка из магазина (SCANNER_REQUIRES_SUBSCRIPTION=true) — только при активной подписке.
     */
    val hasScannerAccess: Flow<Boolean> = subscription.map { data ->
        val now = System.currentTimeMillis()
        if (now < SUBSCRIPTION_REQUIRED_FROM_MILLIS) true
        else if (!scannerRequiresSubscription) true
        else (data.isActive && data.endDate > now)
    }

    /** До [SUBSCRIPTION_REQUIRED_FROM_MILLIS] подписка не требуется. */
    fun isSubscriptionRequiredNow(): Boolean = System.currentTimeMillis() >= SUBSCRIPTION_REQUIRED_FROM_MILLIS

    /**
     * Режим «только чтение» без активной подписки: нельзя создавать/редактировать проекты и комнаты.
     * Просмотр и экспорт/шаринг ранее созданных смет и актов (PDF, CSV) разрешён.
     */
    val isReadOnly: Flow<Boolean> = subscription.map { data ->
        val now = System.currentTimeMillis()
        if (now < SUBSCRIPTION_REQUIRED_FROM_MILLIS) false else !data.isActive || data.endDate < now
    }

    val trialDaysRemaining: Flow<Int?> = subscription.map { data ->
        if (data.type != SubscriptionType.TRIAL) return@map null
        val end = data.endDate
        val now = System.currentTimeMillis()
        val daysLeft = ((end - now) / (24 * 60 * 60 * 1000)).toInt()
        daysLeft.coerceAtLeast(0)
    }

    suspend fun startTrial() {
        preferencesDataStore.ensureFirstLaunchDate()
        preferencesDataStore.ensureDefaultCurrencyFromLocale()
        val firstLaunch = preferencesDataStore.getFirstLaunchDate() ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = firstLaunch }
        // Длительность пробного периода:
        // для ИП/ООО — 30 дней, для остальных — 14 дней
        val businessType = preferencesDataStore.getUserBusinessType()
        val trialDays = if (businessType == "ИП" || businessType == "ООО") 30 else 14
        calendar.add(Calendar.DAY_OF_YEAR, trialDays)
        val endDate = calendar.timeInMillis
        preferencesDataStore.updateSubscription(
            SubscriptionData(
                type = SubscriptionType.TRIAL,
                startDate = firstLaunch,
                endDate = endDate,
                isActive = true
            )
        )
    }

    suspend fun activatePromoCode(code: String): Result<Int> {
        val months = PROMO_CODES[code.uppercase()] ?: return Result.failure(IllegalArgumentException("Неверный промокод"))
        if (preferencesDataStore.isPromoCodeUsed(code)) {
            return Result.failure(IllegalArgumentException("Промокод уже использован"))
        }
        preferencesDataStore.addUsedPromoCode(code)
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        calendar.add(Calendar.MONTH, months)
        preferencesDataStore.updateSubscription(
            SubscriptionData(
                type = when (months) {
                    3 -> SubscriptionType.MONTHLY
                    6 -> SubscriptionType.HALF_YEAR
                    12 -> SubscriptionType.YEARLY
                    else -> SubscriptionType.MONTHLY
                },
                startDate = now,
                endDate = calendar.timeInMillis,
                isActive = true
            )
        )
        return Result.success(months)
    }

    suspend fun purchaseSubscription(durationMonths: Int) {
        val type = when (durationMonths) {
            1 -> SubscriptionType.MONTHLY
            6 -> SubscriptionType.HALF_YEAR
            12 -> SubscriptionType.YEARLY
            else -> SubscriptionType.MONTHLY
        }
        activateSubscription(type, durationMonths)
    }

    /**
     * Активирует подписку после покупки через Google Play: верифицирует на сервере (если настроен),
     * затем сохраняет подписку локально.
     */
    suspend fun activatePurchase(purchase: Purchase, durationMonths: Int): Result<Unit> {
        val verification = purchaseVerificationApi.verify(purchase)
        return verification.fold(
            onSuccess = { result ->
                if (!result.valid) {
                    return Result.failure(Exception("Покупка не подтверждена сервером"))
                }
                val type = when (durationMonths) {
                    1 -> SubscriptionType.MONTHLY
                    6 -> SubscriptionType.HALF_YEAR
                    12 -> SubscriptionType.YEARLY
                    else -> SubscriptionType.MONTHLY
                }
                val now = System.currentTimeMillis()
                val endDate = result.endDateMillis ?: run {
                    val calendar = Calendar.getInstance().apply { timeInMillis = now }
                    calendar.add(Calendar.MONTH, durationMonths)
                    calendar.timeInMillis
                }
                preferencesDataStore.updateSubscription(
                    SubscriptionData(
                        type = type,
                        startDate = now,
                        endDate = endDate,
                        isActive = true
                    )
                )
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun activateSubscription(type: SubscriptionType, durationMonths: Int) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        calendar.add(Calendar.MONTH, durationMonths)
        preferencesDataStore.updateSubscription(
            SubscriptionData(
                type = type,
                startDate = now,
                endDate = calendar.timeInMillis,
                isActive = true
            )
        )
    }
}
