package ru.profia.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.local.entity.RoomScanEntity
import ru.profia.app.data.repository.ProjectRepository
import ru.profia.app.data.repository.RoomScanRepository
import ru.profia.app.data.repository.ScanProcessingRepository
import ru.profia.app.data.remote.ScanDimensionsResult
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RoomScanViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val roomScanRepository: RoomScanRepository,
    private val scanProcessingRepository: ScanProcessingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val projectId: String = savedStateHandle.get<String>("projectId") ?: ""
    val roomId: String = savedStateHandle.get<String>("roomId") ?: "new"

    private val _projectName = MutableStateFlow<String?>(null)
    val projectName: StateFlow<String?> = _projectName.asStateFlow()

    private val _currentScan = MutableStateFlow<RoomScanEntity?>(null)
    val currentScan: StateFlow<RoomScanEntity?> = _currentScan.asStateFlow()

    private val _serverProcessing = MutableStateFlow(false)
    val serverProcessing: StateFlow<Boolean> = _serverProcessing.asStateFlow()

    private val _lastProcessingError = MutableStateFlow<String?>(null)
    val lastProcessingError: StateFlow<String?> = _lastProcessingError.asStateFlow()

    init {
        viewModelScope.launch {
            _projectName.value = projectRepository.getProjectWithRooms(projectId)?.displayName
            val scan = roomScanRepository.createScan(projectId, roomId)
            _currentScan.value = scan
        }
    }

    /** Обновить скан при выходе с экрана (обновить updatedAt). */
    fun onScanSessionEnd() {
        _currentScan.value?.let { scan ->
            viewModelScope.launch {
                roomScanRepository.updateScan(scan)
            }
        }
    }

    /** Отметить скан как готовый (например, после сохранения кадров). */
    fun markScanReady() {
        _currentScan.value?.id?.let { scanId ->
            viewModelScope.launch {
                roomScanRepository.markScanReady(scanId)
                _currentScan.value = roomScanRepository.getScanById(scanId)
            }
        }
    }

    suspend fun processScanOnServer(
        frameFiles: List<File>,
        trajectoryJson: String
    ): Result<ScanDimensionsResult> {
        val scanId = _currentScan.value?.id ?: return Result.failure(
            IllegalStateException("Scan session not initialized")
        )
        _serverProcessing.value = true
        _lastProcessingError.value = null
        val result = scanProcessingRepository.processScan(
            projectId = projectId,
            roomId = roomId,
            scanId = scanId,
            frameFiles = frameFiles,
            trajectoryJson = trajectoryJson,
            depthFiles = null
        )
        _serverProcessing.value = false
        _lastProcessingError.value = result.exceptionOrNull()?.message
        return result
    }

    suspend fun finishScanOnServer(): Result<ScanDimensionsResult> {
        val scanId = _currentScan.value?.id ?: return Result.failure(
            IllegalStateException("Scan session not initialized")
        )
        return scanProcessingRepository.finishScan(
            projectId = projectId,
            roomId = roomId,
            scanId = scanId
        )
    }
}
