package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.repository.SupportRepository
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val supportRepository: SupportRepository
) : ViewModel() {
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    suspend fun submit(
        phone: String,
        email: String,
        description: String
    ): Result<Unit> {
        _isSubmitting.value = true
        _errorMessage.value = null
        val result = supportRepository.submitTicket(phone, email, description)
        _isSubmitting.value = false
        if (result.isFailure) {
            _errorMessage.value = result.exceptionOrNull()?.message ?: ""
        }
        return result
    }

    fun submitAsync(
        phone: String,
        email: String,
        description: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(submit(phone, email, description))
        }
    }
}
