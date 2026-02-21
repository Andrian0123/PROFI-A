package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.repository.AuthAccountRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authAccountRepository: AuthAccountRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(
        login: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        submitAuthRequest(
            request = { authAccountRepository.login(login, password) },
            onSuccess = onSuccess
        )
    }

    fun register(
        login: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        submitAuthRequest(
            request = { authAccountRepository.register(login, password) },
            onSuccess = onSuccess
        )
    }

    private fun submitAuthRequest(
        request: suspend () -> Result<*>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = request()
            _isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}
