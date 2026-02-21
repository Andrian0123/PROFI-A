package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.model.ProjectData
import ru.profia.app.data.repository.ProjectRepository
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _project = MutableStateFlow<ProjectData?>(null)
    val project: StateFlow<ProjectData?> = _project.asStateFlow()

    private val _loadError = MutableStateFlow(false)
    val loadError: StateFlow<Boolean> = _loadError.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalProjectCost = MutableStateFlow(0.0)
    val totalProjectCost: StateFlow<Double> = _totalProjectCost.asStateFlow()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = false
            val data = repository.getProjectWithRooms(projectId)
            _project.value = data
            _isLoading.value = false
            if (data == null) {
                _loadError.value = true
            } else {
                val workItems = repository.getWorkItemsByProjectIdSync(projectId)
                _totalProjectCost.value = workItems.sumOf { it.total }
            }
        }
    }
}
