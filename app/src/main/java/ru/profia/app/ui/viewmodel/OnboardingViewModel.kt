package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.profia.app.data.repository.AuthAccountRepository
import ru.profia.app.data.repository.PreferencesRepository
import ru.profia.app.data.repository.SubscriptionRepository
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val authAccountRepository: AuthAccountRepository
) : ViewModel() {

    fun saveSpecialty(specialty: String, onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setUserSpecialty(specialty)
            onDone()
        }
    }

    fun saveBusinessType(businessType: String, onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setUserBusinessType(businessType)
            onDone()
        }
    }

    /** После выбора кабинета (Профи или ИП/ООО): сохраняет тип и завершает онбординг. */
    fun saveAccountTypeAndComplete(accountType: String, onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setUserAccountType(accountType)
            preferencesRepository.setUserBusinessType(
                if (accountType == "BUSINESS") "IP" else "PROFI"
            )
            preferencesRepository.setOnboardingCompleted(true)
            subscriptionRepository.startTrial()
            onDone()
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
            subscriptionRepository.startTrial()
            onDone()
        }
    }

    suspend fun isOnboardingCompleted(): Boolean =
        preferencesRepository.isOnboardingCompleted()

    suspend fun hasAuthSession(): Boolean = authAccountRepository.hasSession()

    /** Выход из аккаунта: очищает сессию, сбрасывает онбординг и возвращает на экран входа. */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authAccountRepository.logout()
            preferencesRepository.setOnboardingCompleted(false)
            onDone()
        }
    }
}
