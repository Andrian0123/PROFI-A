package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.profia.app.data.model.AppSettings
import ru.profia.app.data.repository.PreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            preferencesRepository.ensureDefaultCurrencyFromLocale()
        }
    }

    val appSettings: StateFlow<AppSettings> = preferencesRepository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            preferencesRepository.updateAppSettings(settings)
        }
    }

    /** Сохраняет настройки и завершается только после записи (для смены языка и перезапуска экрана). */
    suspend fun updateSettingsAndWait(settings: AppSettings) {
        preferencesRepository.updateAppSettings(settings)
    }
}
