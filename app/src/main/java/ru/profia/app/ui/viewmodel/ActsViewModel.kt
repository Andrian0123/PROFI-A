package ru.profia.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.data.model.ProjectData
import ru.profia.app.data.model.UserProfile
import ru.profia.app.data.repository.PreferencesRepository
import ru.profia.app.data.repository.ProjectRepository
import javax.inject.Inject

@HiltViewModel
class ActsViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val projectId: String = savedStateHandle.get<String>("projectId") ?: ""

    private val _acts = MutableStateFlow<List<IntermediateEstimateActEntity>>(emptyList())
    val acts: StateFlow<List<IntermediateEstimateActEntity>> = _acts.asStateFlow()

    private val _actItems = MutableStateFlow<List<IntermediateEstimateActItemEntity>>(emptyList())
    val actItems: StateFlow<List<IntermediateEstimateActItemEntity>> = _actItems.asStateFlow()

    /** Данные проекта (заказчик) для экспорта акта — слева. */
    private val _project = MutableStateFlow<ProjectData?>(null)
    val project: StateFlow<ProjectData?> = _project.asStateFlow()

    /** Профиль (исполнитель) для экспорта акта — справа. В режиме Профи: ФИО, тел., email. */
    val userProfile: StateFlow<UserProfile?> = preferencesRepository.userProfile
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)

    /** Тип кабинета: PROFI — только профиль в акте, BUSINESS — профиль + компания + реквизиты. */
    val userAccountType: StateFlow<String> = preferencesRepository.userAccountType
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "PROFI")

    suspend fun isExportDisclaimerSeen(): Boolean = preferencesRepository.isExportDisclaimerSeen()
    suspend fun markExportDisclaimerSeen() = preferencesRepository.setExportDisclaimerSeen()

    fun loadActs() {
        viewModelScope.launch {
            _acts.value = repository.getIntermediateEstimateActsSync(projectId)
            _project.value = repository.getProjectWithRooms(projectId)
        }
    }

    fun loadActItems(actId: String) {
        viewModelScope.launch {
            _actItems.value = repository.getIntermediateEstimateActItems(actId)
        }
    }

    fun clearActItems() {
        _actItems.value = emptyList()
    }

    fun updateActItem(item: IntermediateEstimateActItemEntity) {
        viewModelScope.launch {
            repository.updateIntermediateEstimateActItem(item)
            loadActItems(item.actId)
        }
    }

    /** Удаляет промежуточную смету (акт). После удаления виды работ из акта снова отображаются в предварительной смете. */
    fun deleteAct(actId: String) {
        viewModelScope.launch {
            repository.deleteIntermediateEstimateAct(actId)
            loadActs()
            if (_actItems.value.any { it.actId == actId }) {
                _actItems.value = emptyList()
            }
        }
    }
}
