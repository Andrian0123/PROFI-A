package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.model.ProjectFormData
import ru.profia.app.data.repository.ProjectRepository
import javax.inject.Inject

@HiltViewModel
class EditProjectViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _form = MutableStateFlow<ProjectFormData?>(null)
    val form: StateFlow<ProjectFormData?> = _form.asStateFlow()

    private val _projectName = MutableStateFlow<String?>(null)
    val projectName: StateFlow<String?> = _projectName.asStateFlow()

    fun load(projectId: String) {
        viewModelScope.launch {
            _projectName.value = repository.getProjectWithRooms(projectId)?.displayName
            _form.value = repository.getProjectForm(projectId)
        }
    }

    fun save(projectId: String, data: ProjectFormData, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateProject(projectId, data)
            onDone()
        }
    }
}

