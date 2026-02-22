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
import ru.profia.app.data.local.entity.RoomWorkItemEntity
import ru.profia.app.data.model.ProjectData
import ru.profia.app.data.model.UserProfile
import ru.profia.app.data.repository.PreferencesRepository
import ru.profia.app.data.repository.ProjectRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class EstimateItemUi(
    val roomName: String,
    val item: RoomWorkItemEntity
)

/** Секция сметы по одному помещению (порядок — по созданию помещений; виды работ внутри секции — по блокам). */
data class EstimateRoomSection(
    val roomName: String,
    val items: List<EstimateItemUi>
)

/** Порядок блоков: потолок → стены → пол → остальные. Используется для сортировки видов работ в смете. */
private val WORK_BLOCK_ORDER = listOf(
    "Потолок", "Стены", "Пол", "Двери", "Окна", "Сантехника", "Электрика", "Вентиляция", "Прочие работы"
)

private fun workBlockOrder(category: String): Int {
    val idx = WORK_BLOCK_ORDER.indexOf(category)
    return if (idx >= 0) idx else WORK_BLOCK_ORDER.size
}

@HiltViewModel
class GeneralEstimateViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val projectId: String = savedStateHandle.get<String>("projectId") ?: ""

    private val _sections = MutableStateFlow<List<EstimateRoomSection>>(emptyList())
    val sections: StateFlow<List<EstimateRoomSection>> = _sections.asStateFlow()

    private val _totalSum = MutableStateFlow(0.0)
    val totalSum: StateFlow<Double> = _totalSum.asStateFlow()

    /** Данные проекта (заказчик) для шапки экспорта сметы. */
    private val _project = MutableStateFlow<ProjectData?>(null)
    val project: StateFlow<ProjectData?> = _project.asStateFlow()

    /** Профиль пользователя (исполнитель) для шапки экспорта сметы. */
    val userProfile: StateFlow<UserProfile?> = preferencesRepository.userProfile
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)

    /** PROFI — в смете только профиль; BUSINESS — профиль + компания + реквизиты. */
    val userAccountType: StateFlow<String> = preferencesRepository.userAccountType
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "PROFI")

    /** Id видов работ, уже входящих в какой-либо акт (исключаются из выбора при создании новой промежуточной сметы). */
    private val _workItemIdsInActs = MutableStateFlow<Set<String>>(emptySet())
    val workItemIdsInActs: StateFlow<Set<String>> = _workItemIdsInActs.asStateFlow()

    /** Акты (промежуточные сметы) текущего проекта — для экспорта КС-2/КС-3 с экрана сметы. */
    private val _acts = MutableStateFlow<List<IntermediateEstimateActEntity>>(emptyList())
    val acts: StateFlow<List<IntermediateEstimateActEntity>> = _acts.asStateFlow()

    suspend fun isExportDisclaimerSeen(): Boolean = preferencesRepository.isExportDisclaimerSeen()
    suspend fun markExportDisclaimerSeen() = preferencesRepository.setExportDisclaimerSeen()

    private val _actItems = MutableStateFlow<List<IntermediateEstimateActItemEntity>>(emptyList())
    val actItems: StateFlow<List<IntermediateEstimateActItemEntity>> = _actItems.asStateFlow()

    fun loadActsForProject() {
        viewModelScope.launch {
            _acts.value = repository.getIntermediateEstimateActsSync(projectId)
            _actItems.value = emptyList()
        }
    }

    fun setSelectedActForExport(actId: String?) {
        viewModelScope.launch {
            if (actId != null) {
                _actItems.value = repository.getIntermediateEstimateActItems(actId)
            } else {
                _actItems.value = emptyList()
            }
        }
    }

    fun loadEstimate() {
        viewModelScope.launch {
            val project = repository.getProjectWithRooms(projectId) ?: run {
                _sections.value = emptyList()
                _totalSum.value = 0.0
                _project.value = null
                return@launch
            }
            _project.value = project
            val roomsOrdered = project.rooms.sortedBy { it.createdAt }
            val workItems = repository.getWorkItemsByProjectIdSync(projectId)
            val itemsByRoom = workItems.groupBy { it.roomId }
            val sectionsList = roomsOrdered
                .filter { itemsByRoom.containsKey(it.id) }
                .map { room ->
                    val roomItems = (itemsByRoom[room.id] ?: emptyList())
                        .sortedWith(compareBy({ workBlockOrder(it.category) }, { it.name }))
                    EstimateRoomSection(
                        roomName = room.name,
                        items = roomItems.map { EstimateItemUi(room.name, it) }
                    )
                }
            _sections.value = sectionsList
            _totalSum.value = workItems.sumOf { it.total }
            _workItemIdsInActs.value = repository.getWorkItemIdsAlreadyInActsSync(projectId)
        }
    }

    fun deleteWorkItem(id: String) {
        viewModelScope.launch {
            repository.deleteWorkItem(id)
            loadEstimate()
        }
    }

    fun updateWorkItem(entity: RoomWorkItemEntity) {
        viewModelScope.launch {
            repository.updateWorkItem(entity)
            loadEstimate()
        }
    }

    /** Сохраняет выбранные позиции как промежуточную смету (акт). */
    fun saveIntermediateEstimateAct(selectedItems: List<EstimateItemUi>) {
        if (selectedItems.isEmpty()) return
        viewModelScope.launch {
            val title = "Промежуточная смета " + SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
            repository.saveIntermediateEstimateAct(
                projectId = projectId,
                title = title,
                items = selectedItems.map { it.roomName to it.item }
            )
            loadEstimate()
        }
    }
}
