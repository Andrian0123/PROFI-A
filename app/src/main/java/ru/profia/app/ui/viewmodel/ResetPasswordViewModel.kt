package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.remote.ResetPasswordRequestResult
import ru.profia.app.data.repository.AuthAccountRepository
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authAccountRepository: AuthAccountRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _requestResult = MutableStateFlow<ResetPasswordRequestResult?>(null)
    val requestResult: StateFlow<ResetPasswordRequestResult?> = _requestResult.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearRequestResult() {
        _requestResult.value = null
    }

    fun requestReset(loginOrEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _requestResult.value = null
            val result = authAccountRepository.requestResetPassword(loginOrEmail.trim())
            _isLoading.value = false
            result.fold(
                onSuccess = { _requestResult.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authAccountRepository.resetPassword(token.trim(), newPassword)
            _isLoading.value = false
            result.fold(
                onSuccess = { _resetSuccess.value = true },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }
}
