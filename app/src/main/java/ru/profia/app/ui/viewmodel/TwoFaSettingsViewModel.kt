package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.profia.app.data.repository.AuthAccountRepository
import ru.profia.app.data.repository.PreferencesRepository
import javax.inject.Inject

@HiltViewModel
class TwoFaSettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val authAccountRepository: AuthAccountRepository
) : ViewModel() {
    val enabled: StateFlow<Boolean> = preferencesRepository.twoFaEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setEnabled(enabled: Boolean, onError: () -> Unit = {}) {
        viewModelScope.launch {
            val backendResult = authAccountRepository.setTwoFaEnabled(enabled)
            if (backendResult.isSuccess) {
                preferencesRepository.setTwoFaEnabled(enabled)
            } else {
                onError()
            }
        }
    }
}
