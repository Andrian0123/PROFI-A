package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.profia.app.data.model.UserProfile
import ru.profia.app.data.repository.AuthAccountRepository
import ru.profia.app.data.repository.PreferencesRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val authAccountRepository: AuthAccountRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = preferencesRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val accountType: StateFlow<String> = preferencesRepository.userAccountType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "PROFI")

    val profilePhotoPath: StateFlow<String?> = preferencesRepository.profilePhotoPath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            preferencesRepository.updateUserProfile(profile)
        }
    }

    fun setAccountType(type: String) {
        viewModelScope.launch {
            preferencesRepository.setUserAccountType(type)
        }
    }

    fun setProfilePhotoPath(path: String?) {
        viewModelScope.launch {
            preferencesRepository.setProfilePhotoPath(path)
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authAccountRepository.deleteAccount()
            if (result.isSuccess) {
                authAccountRepository.logout()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Request failed")
            }
        }
    }
}
