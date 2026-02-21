package ru.profia.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.profia.app.data.local.mapper.toRoomFormData
import ru.profia.app.data.model.OpeningFormData
import ru.profia.app.data.model.RoomFormData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ru.profia.app.data.model.RoomWorkItemForm
import ru.profia.app.data.model.SuggestedWorkItem
import ru.profia.app.data.model.WorkTemplate
import ru.profia.app.data.repository.PreferencesRepository
import ru.profia.app.data.repository.ProjectRepository
import ru.profia.app.data.repository.RoomScanRepository
import javax.inject.Inject

@HiltViewModel
class AddRoomViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val roomScanRepository: RoomScanRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val workTemplates = preferencesRepository.workTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectId: String = savedStateHandle.get<String>("projectId") ?: ""
    val roomId: String? = savedStateHandle.get<String>("roomId")?.takeIf { it != "new" }

    private val _initialForm = MutableStateFlow<RoomFormData?>(null)
    val initialForm: StateFlow<RoomFormData?> = _initialForm.asStateFlow()

    private val _initialOpenings = MutableStateFlow<List<OpeningFormData>>(emptyList())
    val initialOpenings: StateFlow<List<OpeningFormData>> = _initialOpenings.asStateFlow()

    private val _initialWorkItems = MutableStateFlow<Map<String, List<RoomWorkItemForm>>?>(null)
    val initialWorkItems: StateFlow<Map<String, List<RoomWorkItemForm>>?> = _initialWorkItems.asStateFlow()

    private val _suggestedWorkItems = MutableStateFlow<List<SuggestedWorkItem>>(emptyList())
    val suggestedWorkItems: StateFlow<List<SuggestedWorkItem>> = _suggestedWorkItems.asStateFlow()

    private val _projectName = MutableStateFlow<String?>(null)
    val projectName: StateFlow<String?> = _projectName.asStateFlow()

    init {
        viewModelScope.launch {
            _projectName.value = repository.getProjectWithRooms(projectId)?.displayName
            if (roomId != null) {
                val entity = repository.getRoomById(roomId!!)
                _initialForm.value = entity?.toRoomFormData()
                _initialOpenings.value = repository.getOpeningsByRoomId(roomId!!)
                _initialWorkItems.value = repository.getWorkItemsByRoomIdSync(roomId!!)
            } else {
                val suggested = repository.getSuggestedRoomForm(projectId)
                if (suggested != null) {
                    _initialForm.value = suggested.second.copy(name = suggested.first)
                    repository.clearSuggestedRoomForm(projectId)
                }
            }
            _suggestedWorkItems.value = repository.getSuggestedWorkItemsFromOtherRooms(projectId, roomId)
        }
    }

    /** Сохраняет путь к загруженной 3D-модели для текущей комнаты. */
    fun saveMeshPath(projectId: String, roomId: String, meshPath: String) {
        viewModelScope.launch {
            roomScanRepository.setMeshPathForRoom(projectId, roomId, meshPath)
        }
    }
}
