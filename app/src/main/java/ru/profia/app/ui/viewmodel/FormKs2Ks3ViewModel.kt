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
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.data.model.Project
import ru.profia.app.data.model.UserProfile
import ru.profia.app.data.repository.PreferencesRepository
import ru.profia.app.data.repository.ProjectRepository
import javax.inject.Inject

@HiltViewModel
class FormKs2Ks3ViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val projects: StateFlow<List<Project>> = projectRepository.getProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = preferencesRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** PROFI — в КС-2/КС-3 только профиль; BUSINESS — компания и реквизиты. */
    val userAccountType: StateFlow<String> = preferencesRepository.userAccountType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "PROFI")

    private val _acts = MutableStateFlow<List<IntermediateEstimateActEntity>>(emptyList())
    val acts: StateFlow<List<IntermediateEstimateActEntity>> = _acts.asStateFlow()

    private val _actItems = MutableStateFlow<List<IntermediateEstimateActItemEntity>>(emptyList())
    val actItems: StateFlow<List<IntermediateEstimateActItemEntity>> = _actItems.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    private val _selectedActId = MutableStateFlow<String?>(null)
    val selectedActId: StateFlow<String?> = _selectedActId.asStateFlow()

    fun setSelectedProject(projectId: String?) {
        _selectedProjectId.value = projectId
        _selectedActId.value = null
        _actItems.value = emptyList()
        projectId?.let { loadActs(it) } ?: run { _acts.value = emptyList() }
    }

    fun setSelectedAct(actId: String?) {
        _selectedActId.value = actId
        actId?.let { loadActItems(it) } ?: run { _actItems.value = emptyList() }
    }

    fun loadActs(projectId: String) {
        viewModelScope.launch {
            _acts.value = projectRepository.getIntermediateEstimateActsSync(projectId)
        }
    }

    fun loadActItems(actId: String) {
        viewModelScope.launch {
            _actItems.value = projectRepository.getIntermediateEstimateActItems(actId)
        }
    }
}
