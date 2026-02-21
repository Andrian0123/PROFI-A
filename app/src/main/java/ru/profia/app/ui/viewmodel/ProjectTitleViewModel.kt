package ru.profia.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.repository.ProjectRepository
import javax.inject.Inject

/** ViewModel для экранов с projectId в маршруте: предоставляет название проекта для отображения в шапке. */
@HiltViewModel
class ProjectTitleViewModel @Inject constructor(
    private val repository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: String = savedStateHandle.get<String>("projectId") ?: ""

    private val _projectName = MutableStateFlow<String?>(null)
    val projectName: StateFlow<String?> = _projectName.asStateFlow()

    init {
        viewModelScope.launch {
            _projectName.value = repository.getProjectWithRooms(projectId)?.displayName
        }
    }
}
