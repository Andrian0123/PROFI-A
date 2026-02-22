package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.profia.app.data.remote.DocumentScanResult
import ru.profia.app.data.repository.ScanProcessingRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DocumentScanViewModel @Inject constructor(
    private val scanProcessingRepository: ScanProcessingRepository
) : ViewModel() {

    private val _scanId = MutableStateFlow(UUID.randomUUID().toString())
    val scanId: StateFlow<String> = _scanId.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _result = MutableStateFlow<DocumentScanResult?>(null)
    val result: StateFlow<DocumentScanResult?> = _result.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun scanDocument(documentFile: File) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _result.value = null
            val result = scanProcessingRepository.scanDocument(_scanId.value, documentFile)
            _loading.value = false
            result.fold(
                onSuccess = { _result.value = it },
                onFailure = { _error.value = it.message }
            )
            // Удаляем временный файл после использования, чтобы не забивать кэш
            withContext(Dispatchers.IO) {
                try { if (documentFile.exists()) documentFile.delete() } catch (_: Exception) { }
            }
        }
    }

    fun clearResult() {
        _result.value = null
        _error.value = null
        _scanId.value = UUID.randomUUID().toString()
    }
}
