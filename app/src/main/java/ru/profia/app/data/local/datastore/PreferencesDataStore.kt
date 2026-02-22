package ru.profia.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.profia.app.data.model.AppSettings
import ru.profia.app.data.model.SubscriptionType
import ru.profia.app.data.model.WorkTemplate
import ru.profia.app.data.remote.AuthSession

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "profia_preferences")

object PreferenceKeys {
    val AUTH_USER_ID = stringPreferencesKey("auth_user_id")
    val AUTH_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
    val AUTH_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")

    val SUBSCRIPTION_TYPE = stringPreferencesKey("subscription_type")
    val SUBSCRIPTION_START = longPreferencesKey("subscription_start")
    val SUBSCRIPTION_END = longPreferencesKey("subscription_end")
    val SUBSCRIPTION_ACTIVE = booleanPreferencesKey("subscription_active")
    val FIRST_LAUNCH_DATE = longPreferencesKey("first_launch_date")

    val CITY = stringPreferencesKey("city")
    val CURRENCY = stringPreferencesKey("currency")
    val LANGUAGE = stringPreferencesKey("language")
    val UNIT_SYSTEM = stringPreferencesKey("unit_system")

    val USER_LAST_NAME = stringPreferencesKey("user_last_name")
    val USER_FIRST_NAME = stringPreferencesKey("user_first_name")
    val USER_MIDDLE_NAME = stringPreferencesKey("user_middle_name")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_PHONE = stringPreferencesKey("user_phone")
    val COMPANY_NAME = stringPreferencesKey("company_name")
    val INN = stringPreferencesKey("inn")
    val KPP = stringPreferencesKey("kpp")
    val LEGAL_ADDRESS = stringPreferencesKey("legal_address")
    val BANK_NAME = stringPreferencesKey("bank_name")
    val ACCOUNT_NUMBER = stringPreferencesKey("account_number")
    val CORRESPONDENT_ACCOUNT = stringPreferencesKey("correspondent_account")
    val BIC = stringPreferencesKey("bic")

    val USED_PROMO_CODES = stringPreferencesKey("used_promo_codes")

    val USER_SPECIALTY = stringPreferencesKey("user_specialty")
    val USER_BUSINESS_TYPE = stringPreferencesKey("user_business_type")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val USER_ACCOUNT_TYPE = stringPreferencesKey("user_account_type")
    val TWO_FA_ENABLED = booleanPreferencesKey("two_fa_enabled")
    val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
    val EXPORT_DISCLAIMER_SEEN = booleanPreferencesKey("export_disclaimer_seen")

    val WORK_TEMPLATES = stringPreferencesKey("work_templates")
}

private fun String.sanitizeForWorkTemplate(): String = replace("|", " ").replace("\n", " ").trim()

class PreferencesDataStore(private val context: Context) {
    private val dataStore = context.dataStore

    val appSettings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            city = prefs[PreferenceKeys.CITY] ?: "Краснодар",
            currency = prefs[PreferenceKeys.CURRENCY] ?: "RUR",
            language = prefs[PreferenceKeys.LANGUAGE] ?: "RU", // русский по умолчанию
            unitSystem = prefs[PreferenceKeys.UNIT_SYSTEM] ?: "Метры"
        )
    }

    val userProfileFlow: Flow<ru.profia.app.data.model.UserProfile?> = dataStore.data.map { prefs ->
        val profile = ru.profia.app.data.model.UserProfile(
            lastName = prefs[PreferenceKeys.USER_LAST_NAME] ?: "",
            firstName = prefs[PreferenceKeys.USER_FIRST_NAME] ?: "",
            middleName = prefs[PreferenceKeys.USER_MIDDLE_NAME],
            email = prefs[PreferenceKeys.USER_EMAIL] ?: "",
            phone = prefs[PreferenceKeys.USER_PHONE] ?: "",
            companyName = prefs[PreferenceKeys.COMPANY_NAME],
            inn = prefs[PreferenceKeys.INN],
            kpp = prefs[PreferenceKeys.KPP],
            legalAddress = prefs[PreferenceKeys.LEGAL_ADDRESS],
            bankName = prefs[PreferenceKeys.BANK_NAME],
            accountNumber = prefs[PreferenceKeys.ACCOUNT_NUMBER],
            correspondentAccount = prefs[PreferenceKeys.CORRESPONDENT_ACCOUNT],
            bic = prefs[PreferenceKeys.BIC]
        )
        if (profile.email.isEmpty() && profile.phone.isEmpty() &&
            profile.lastName.isEmpty() && profile.firstName.isEmpty() &&
            profile.companyName.isNullOrBlank() && profile.inn.isNullOrBlank() &&
            profile.bankName.isNullOrBlank() && profile.accountNumber.isNullOrBlank()) {
            null
        } else {
            profile
        }
    }

    val userAccountTypeFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.USER_ACCOUNT_TYPE] ?: "PROFI"
    }

    val twoFaEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.TWO_FA_ENABLED] ?: false
    }

    val profilePhotoPathFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.PROFILE_PHOTO_PATH]?.takeIf { it.isNotEmpty() }
    }

    val subscriptionFlow: Flow<ru.profia.app.data.model.SubscriptionData> = dataStore.data.map { prefs ->
        val typeStr = prefs[PreferenceKeys.SUBSCRIPTION_TYPE] ?: SubscriptionType.NONE.name
        val type = try {
            SubscriptionType.valueOf(typeStr)
        } catch (e: Exception) {
            SubscriptionType.NONE
        }
        ru.profia.app.data.model.SubscriptionData(
            type = type,
            startDate = prefs[PreferenceKeys.SUBSCRIPTION_START] ?: 0L,
            endDate = prefs[PreferenceKeys.SUBSCRIPTION_END] ?: 0L,
            isActive = prefs[PreferenceKeys.SUBSCRIPTION_ACTIVE] ?: false
        )
    }

    suspend fun updateAppSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.CITY] = settings.city
            prefs[PreferenceKeys.CURRENCY] = settings.currency
            prefs[PreferenceKeys.LANGUAGE] = settings.language
            prefs[PreferenceKeys.UNIT_SYSTEM] = settings.unitSystem
        }
    }

    suspend fun updateSubscription(data: ru.profia.app.data.model.SubscriptionData) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SUBSCRIPTION_TYPE] = data.type.name
            prefs[PreferenceKeys.SUBSCRIPTION_START] = data.startDate
            prefs[PreferenceKeys.SUBSCRIPTION_END] = data.endDate
            prefs[PreferenceKeys.SUBSCRIPTION_ACTIVE] = data.isActive
        }
    }

    suspend fun getFirstLaunchDate(): Long? =
        dataStore.data.first()[PreferenceKeys.FIRST_LAUNCH_DATE]

    suspend fun setFirstLaunchDate(date: Long) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FIRST_LAUNCH_DATE] = date
        }
    }

    suspend fun ensureFirstLaunchDate() {
        dataStore.edit { prefs ->
            if (prefs[PreferenceKeys.FIRST_LAUNCH_DATE] == null) {
                prefs[PreferenceKeys.FIRST_LAUNCH_DATE] = System.currentTimeMillis()
            }
        }
    }

    /** Устанавливает валюту по умолчанию по локали устройства, если значение ещё не задано. */
    suspend fun ensureDefaultCurrencyFromLocale() {
        dataStore.edit { prefs ->
            if (prefs[PreferenceKeys.CURRENCY] == null) {
                prefs[PreferenceKeys.CURRENCY] = when (Locale.getDefault().country) {
                    "RU", "BY", "KZ" -> "RUR"
                    else -> "RUR"
                }
            }
        }
    }

    suspend fun updateUserProfile(profile: ru.profia.app.data.model.UserProfile) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.USER_LAST_NAME] = profile.lastName
            prefs[PreferenceKeys.USER_FIRST_NAME] = profile.firstName
            profile.middleName?.let { prefs[PreferenceKeys.USER_MIDDLE_NAME] = it }
            prefs[PreferenceKeys.USER_EMAIL] = profile.email
            prefs[PreferenceKeys.USER_PHONE] = profile.phone
            profile.companyName?.let { prefs[PreferenceKeys.COMPANY_NAME] = it }
            profile.inn?.let { prefs[PreferenceKeys.INN] = it }
            profile.kpp?.let { prefs[PreferenceKeys.KPP] = it }
            profile.legalAddress?.let { prefs[PreferenceKeys.LEGAL_ADDRESS] = it }
            profile.bankName?.let { prefs[PreferenceKeys.BANK_NAME] = it }
            profile.accountNumber?.let { prefs[PreferenceKeys.ACCOUNT_NUMBER] = it }
            profile.correspondentAccount?.let { prefs[PreferenceKeys.CORRESPONDENT_ACCOUNT] = it }
            profile.bic?.let { prefs[PreferenceKeys.BIC] = it }
        }
    }

    suspend fun addUsedPromoCode(code: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.USED_PROMO_CODES] ?: ""
            prefs[PreferenceKeys.USED_PROMO_CODES] = if (current.isEmpty()) code else "$current,$code"
        }
    }

    suspend fun isPromoCodeUsed(code: String): Boolean {
        val codes = dataStore.data.first()[PreferenceKeys.USED_PROMO_CODES] ?: ""
        return codes.split(",").contains(code)
    }

    suspend fun setUserSpecialty(specialty: String) {
        dataStore.edit { prefs -> prefs[PreferenceKeys.USER_SPECIALTY] = specialty }
    }

    suspend fun getUserSpecialty(): String? =
        dataStore.data.first()[PreferenceKeys.USER_SPECIALTY]

    suspend fun setUserBusinessType(businessType: String) {
        dataStore.edit { prefs -> prefs[PreferenceKeys.USER_BUSINESS_TYPE] = businessType }
    }

    suspend fun getUserBusinessType(): String? =
        dataStore.data.first()[PreferenceKeys.USER_BUSINESS_TYPE]

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs -> prefs[PreferenceKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun isOnboardingCompleted(): Boolean =
        dataStore.data.first()[PreferenceKeys.ONBOARDING_COMPLETED] ?: false

    suspend fun setAuthSession(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.AUTH_USER_ID] = session.userId
            prefs[PreferenceKeys.AUTH_ACCESS_TOKEN] = session.accessToken
            session.refreshToken?.let { prefs[PreferenceKeys.AUTH_REFRESH_TOKEN] = it }
                ?: prefs.remove(PreferenceKeys.AUTH_REFRESH_TOKEN)
        }
    }

    suspend fun clearAuthSession() {
        dataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.AUTH_USER_ID)
            prefs.remove(PreferenceKeys.AUTH_ACCESS_TOKEN)
            prefs.remove(PreferenceKeys.AUTH_REFRESH_TOKEN)
        }
    }

    suspend fun getAccessToken(): String? =
        dataStore.data.first()[PreferenceKeys.AUTH_ACCESS_TOKEN]?.takeIf { it.isNotBlank() }

    suspend fun hasAuthSession(): Boolean = getAccessToken() != null

    suspend fun setUserAccountType(type: String) {
        dataStore.edit { prefs -> prefs[PreferenceKeys.USER_ACCOUNT_TYPE] = type }
    }

    suspend fun getUserAccountType(): String =
        dataStore.data.first()[PreferenceKeys.USER_ACCOUNT_TYPE] ?: "PROFI"

    suspend fun setTwoFaEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[PreferenceKeys.TWO_FA_ENABLED] = enabled }
    }

    suspend fun isTwoFaEnabled(): Boolean =
        dataStore.data.first()[PreferenceKeys.TWO_FA_ENABLED] ?: false

    suspend fun setProfilePhotoPath(path: String?) {
        dataStore.edit { prefs ->
            if (path.isNullOrBlank()) prefs.remove(PreferenceKeys.PROFILE_PHOTO_PATH)
            else prefs[PreferenceKeys.PROFILE_PHOTO_PATH] = path
        }
    }

    suspend fun isExportDisclaimerSeen(): Boolean =
        dataStore.data.first()[PreferenceKeys.EXPORT_DISCLAIMER_SEEN] ?: false

    suspend fun setExportDisclaimerSeen() {
        dataStore.edit { prefs -> prefs[PreferenceKeys.EXPORT_DISCLAIMER_SEEN] = true }
    }

    val workTemplatesFlow: Flow<List<WorkTemplate>> = dataStore.data.map { prefs ->
        val raw = prefs[PreferenceKeys.WORK_TEMPLATES] ?: ""
        if (raw.isEmpty()) emptyList()
        else raw.split("\n").mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 5) {
                WorkTemplate(
                    id = parts[0],
                    name = parts[1],
                    category = parts[2],
                    unitAbbr = parts[3],
                    defaultPrice = parts[4].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }

    suspend fun addWorkTemplate(template: WorkTemplate) {
        dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.WORK_TEMPLATES] ?: ""
            val line = "${template.id}|${template.name.sanitizeForWorkTemplate()}|${template.category.sanitizeForWorkTemplate()}|${template.unitAbbr.sanitizeForWorkTemplate()}|${template.defaultPrice}"
            prefs[PreferenceKeys.WORK_TEMPLATES] = if (current.isEmpty()) line else "$current\n$line"
        }
    }
}
