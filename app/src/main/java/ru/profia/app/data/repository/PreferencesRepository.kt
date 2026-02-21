package ru.profia.app.data.repository

import kotlinx.coroutines.flow.Flow
import ru.profia.app.data.model.AppSettings
import ru.profia.app.data.model.UserProfile
import ru.profia.app.data.model.WorkTemplate
import ru.profia.app.data.local.datastore.PreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: PreferencesDataStore
) {
    val appSettings: Flow<AppSettings> = dataStore.appSettings
    val userProfile: Flow<UserProfile?> = dataStore.userProfileFlow
    val userAccountType: Flow<String> = dataStore.userAccountTypeFlow
    val twoFaEnabled: Flow<Boolean> = dataStore.twoFaEnabledFlow
    val profilePhotoPath: Flow<String?> = dataStore.profilePhotoPathFlow

    suspend fun updateAppSettings(settings: AppSettings) =
        dataStore.updateAppSettings(settings)

    suspend fun updateUserProfile(profile: UserProfile) =
        dataStore.updateUserProfile(profile)

    suspend fun setUserSpecialty(specialty: String) = dataStore.setUserSpecialty(specialty)
    suspend fun getUserSpecialty(): String? = dataStore.getUserSpecialty()
    suspend fun setUserBusinessType(businessType: String) = dataStore.setUserBusinessType(businessType)
    suspend fun getUserBusinessType(): String? = dataStore.getUserBusinessType()
    suspend fun setOnboardingCompleted(completed: Boolean) = dataStore.setOnboardingCompleted(completed)
    suspend fun isOnboardingCompleted(): Boolean = dataStore.isOnboardingCompleted()

    suspend fun ensureDefaultCurrencyFromLocale() = dataStore.ensureDefaultCurrencyFromLocale()

    suspend fun setUserAccountType(type: String) = dataStore.setUserAccountType(type)
    suspend fun getUserAccountType(): String = dataStore.getUserAccountType()
    suspend fun setTwoFaEnabled(enabled: Boolean) = dataStore.setTwoFaEnabled(enabled)
    suspend fun isTwoFaEnabled(): Boolean = dataStore.isTwoFaEnabled()
    suspend fun setProfilePhotoPath(path: String?) = dataStore.setProfilePhotoPath(path)

    val workTemplates: Flow<List<WorkTemplate>> = dataStore.workTemplatesFlow
    suspend fun addWorkTemplate(template: WorkTemplate) = dataStore.addWorkTemplate(template)
}
