package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.profia.app.data.model.Project
import ru.profia.app.data.model.ProjectFormData
import ru.profia.app.data.model.RoomFormData
import ru.profia.app.data.model.RoomWorkItemForm
import ru.profia.app.data.repository.ProjectRepository
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    val projects: StateFlow<List<Project>> = repository.getProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastCreatedProjectId = MutableStateFlow<String?>(null)
    val lastCreatedProjectId: StateFlow<String?> = _lastCreatedProjectId.asStateFlow()

    private val _createProjectError = MutableStateFlow(false)
    val createProjectError: StateFlow<Boolean> = _createProjectError.asStateFlow()

    fun addProject(data: ProjectFormData, room: RoomFormData?) {
        viewModelScope.launch {
            _createProjectError.value = false
            try {
                val id = repository.addProject(data, room)
                _lastCreatedProjectId.value = id
            } catch (_: Exception) {
                _createProjectError.value = true
            }
        }
    }

    fun clearCreateProjectError() {
        _createProjectError.value = false
    }

    fun addRoom(
        projectId: String,
        room: RoomFormData,
        openings: List<ru.profia.app.data.model.OpeningFormData> = emptyList(),
        workItems: Map<String, List<RoomWorkItemForm>> = emptyMap()
    ) {
        viewModelScope.launch {
            repository.addRoom(projectId, room, openings, workItems)
        }
    }

    fun updateRoom(
        roomId: String,
        room: RoomFormData,
        openings: List<ru.profia.app.data.model.OpeningFormData> = emptyList(),
        workItems: Map<String, List<RoomWorkItemForm>> = emptyMap()
    ) {
        viewModelScope.launch {
            repository.updateRoom(roomId, room, openings, workItems)
        }
    }

    fun clearLastCreatedProjectId() {
        _lastCreatedProjectId.value = null
    }

    fun setSuggestedRoomForm(projectId: String, suggestedName: String, formData: RoomFormData) {
        repository.setSuggestedRoomForm(projectId, suggestedName, formData)
    }

    /**
     * Гарантирует наличие одного демо‑проекта для ознакомления с функциями.
     * Вызывается при открытии главного экрана.
     */
    fun ensureDemoProject() {
        viewModelScope.launch {
            repository.ensureDemoProject()
        }
    }
}
