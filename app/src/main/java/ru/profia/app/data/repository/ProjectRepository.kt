package ru.profia.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.profia.app.data.local.dao.IntermediateEstimateActDao
import ru.profia.app.data.local.dao.OpeningDao
import ru.profia.app.data.local.dao.ProjectDao
import ru.profia.app.data.local.dao.RoomDao
import ru.profia.app.data.local.dao.RoomWorkItemDao
import ru.profia.app.data.local.mapper.toOpeningData
import ru.profia.app.data.local.mapper.toProjectData
import ru.profia.app.data.local.mapper.toRoomData
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.data.local.entity.ProjectEntity
import ru.profia.app.data.local.entity.RoomEntity
import ru.profia.app.data.model.Project
import ru.profia.app.data.model.ProjectData
import ru.profia.app.data.local.entity.OpeningEntity
import ru.profia.app.data.local.entity.RoomWorkItemEntity
import ru.profia.app.data.model.OpeningFormData
import ru.profia.app.data.model.ProjectFormData
import ru.profia.app.data.model.RoomFormData
import ru.profia.app.data.model.RoomWorkItemForm
import ru.profia.app.data.model.SuggestedWorkItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val roomDao: RoomDao,
    private val openingDao: OpeningDao,
    private val roomWorkItemDao: RoomWorkItemDao,
    private val intermediateEstimateActDao: IntermediateEstimateActDao
) {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    /** Подсказка «добавить этаж»: projectId -> (название следующего этажа, форма с теми же параметрами). */
    @Volatile
    private var suggestedFloorByProject: MutableMap<String, Pair<String, RoomFormData>> = mutableMapOf()

    fun setSuggestedRoomForm(projectId: String, suggestedName: String, formData: RoomFormData) {
        suggestedFloorByProject[projectId] = suggestedName to formData
    }

    fun getSuggestedRoomForm(projectId: String): Pair<String, RoomFormData>? =
        suggestedFloorByProject[projectId]

    fun clearSuggestedRoomForm(projectId: String) {
        suggestedFloorByProject.remove(projectId)
    }

    fun getProjects(): Flow<List<Project>> = combine(
        projectDao.getAllProjects(),
        roomDao.getAllRooms()
    ) { projects, allRooms ->
        val roomCountByProject = allRooms.groupBy { it.projectId }.mapValues { it.value.size }
        projects.map { project ->
            Project(
                id = project.id,
                name = "${project.lastName} ${project.firstName}",
                address = project.address,
                date = dateFormat.format(Date(project.updatedAt)),
                cost = "0 ₽",
                roomCount = roomCountByProject[project.id] ?: 0,
                isEditable = true
            )
        }
    }

    suspend fun addProject(data: ProjectFormData, room: RoomFormData?): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val projectEntity = ProjectEntity(
            id = id,
            lastName = data.lastName,
            firstName = data.firstName,
            middleName = data.middleName,
            email = data.email,
            phone = data.phone,
            address = data.address,
            city = data.city,
            street = data.street,
            house = data.house,
            apartment = data.apartment,
            createdAt = now,
            updatedAt = now
        )
        projectDao.insertProject(projectEntity)
        if (room != null) {
            val roomEntity = createRoomEntity(room, id, UUID.randomUUID().toString(), emptyList())
            roomDao.insertRoom(roomEntity)
        }
        return id
    }

    suspend fun getProjectWithRooms(projectId: String): ProjectData? {
        val project = projectDao.getProjectById(projectId) ?: return null
        val rooms = roomDao.getRoomsByProjectIdSync(projectId)
        val roomDataList = rooms.map { room ->
            val openings = openingDao.getOpeningsByRoomId(room.id).map { it.toOpeningData() }
            room.toRoomData(openings)
        }
        return project.toProjectData(roomDataList)
    }

    suspend fun getProjectForm(projectId: String): ProjectFormData? {
        val project = projectDao.getProjectById(projectId) ?: return null
        return ProjectFormData(
            lastName = project.lastName,
            firstName = project.firstName,
            middleName = project.middleName,
            email = project.email,
            phone = project.phone,
            address = project.address,
            city = project.city,
            street = project.street,
            house = project.house,
            apartment = project.apartment
        )
    }

    suspend fun getRoomById(roomId: String) = roomDao.getRoomById(roomId)

    suspend fun getOpeningsByRoomId(roomId: String): List<OpeningFormData> =
        openingDao.getOpeningsByRoomId(roomId).map { e ->
            OpeningFormData(
                type = ru.profia.app.data.model.OpeningType.valueOf(e.type),
                width = e.width,
                height = e.height,
                count = e.count
            )
        }

    suspend fun addRoom(
        projectId: String,
        room: RoomFormData,
        openings: List<OpeningFormData> = emptyList(),
        workItems: Map<String, List<RoomWorkItemForm>> = emptyMap()
    ): String {
        val project = projectDao.getProjectById(projectId) ?: return ""
        val now = System.currentTimeMillis()
        val roomId = UUID.randomUUID().toString()
        val roomEntity = createRoomEntity(room, projectId, roomId, openings)
        roomDao.insertRoom(roomEntity)
        openings.forEach { o ->
            openingDao.insertOpening(OpeningEntity(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                type = o.type.name,
                width = o.width,
                height = o.height,
                count = o.count
            ))
        }
        saveRoomWorkItems(roomId, workItems)
        projectDao.updateProjectTimestamp(projectId, now)
        return roomId
    }

    suspend fun updateRoom(
        roomId: String,
        room: RoomFormData,
        openings: List<OpeningFormData> = emptyList(),
        workItems: Map<String, List<RoomWorkItemForm>> = emptyMap()
    ) {
        val existing = roomDao.getRoomById(roomId) ?: return
        val now = System.currentTimeMillis()
        val roomEntity = createRoomEntity(room, existing.projectId, roomId, openings)
        roomDao.insertRoom(roomEntity)
        openingDao.deleteOpeningsByRoomId(roomId)
        openings.forEach { o ->
            openingDao.insertOpening(OpeningEntity(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                type = o.type.name,
                width = o.width,
                height = o.height,
                count = o.count
            ))
        }
        saveRoomWorkItems(roomId, workItems)
        projectDao.updateProjectTimestamp(existing.projectId, now)
    }

    private suspend fun saveRoomWorkItems(roomId: String, workItems: Map<String, List<RoomWorkItemForm>>) {
        roomWorkItemDao.deleteByRoomId(roomId)
        val entities = workItems.flatMap { (category, list) ->
            list.map { item ->
                RoomWorkItemEntity(
                    id = UUID.randomUUID().toString(),
                    roomId = roomId,
                    category = category,
                    name = item.name,
                    unitAbbr = item.unitAbbr,
                    price = item.price,
                    quantity = item.quantity
                )
            }
        }
        if (entities.isNotEmpty()) roomWorkItemDao.insertAll(entities)
    }

    suspend fun getWorkItemsByRoomIdSync(roomId: String): Map<String, List<RoomWorkItemForm>> {
        val list = roomWorkItemDao.getByRoomIdSync(roomId)
        return list.groupBy { it.category }.mapValues { (_, entities) ->
            entities.map { e ->
                RoomWorkItemForm(
                    category = e.category,
                    name = e.name,
                    unitAbbr = e.unitAbbr,
                    price = e.price,
                    quantity = e.quantity
                )
            }
        }
    }

    suspend fun getWorkItemsByProjectIdSync(projectId: String): List<RoomWorkItemEntity> =
        roomWorkItemDao.getByProjectIdSync(projectId)

    /**
     * Виды работ, использованные в других комнатах проекта (для подсказки при добавлении комнаты).
     * Исключаются позиции текущей комнаты при редактировании.
     * Сортировка: по убыванию числа комнат, где встречается вид работы.
     */
    suspend fun getSuggestedWorkItemsFromOtherRooms(
        projectId: String,
        excludeRoomId: String? = null
    ): List<SuggestedWorkItem> {
        val all = roomWorkItemDao.getByProjectIdSync(projectId)
        val filtered = if (excludeRoomId != null) all.filter { it.roomId != excludeRoomId } else all
        if (filtered.isEmpty()) return emptyList()
        val grouped = filtered.groupBy { Triple(it.category, it.name, it.unitAbbr) }
        return grouped.map { (key, list) ->
            SuggestedWorkItem(
                category = key.first,
                name = key.second,
                unitAbbr = key.third,
                price = list.map { it.price }.average(),
                roomCount = list.map { it.roomId }.toSet().size
            )
        }.sortedByDescending { it.roomCount }
    }

    suspend fun deleteWorkItem(id: String) {
        roomWorkItemDao.deleteById(id)
    }

    suspend fun updateWorkItem(entity: RoomWorkItemEntity) {
        roomWorkItemDao.insert(entity)
    }

    /** Сохраняет промежуточную смету (акт). items: список (название помещения, позиция). */
    suspend fun saveIntermediateEstimateAct(
        projectId: String,
        title: String,
        items: List<Pair<String, RoomWorkItemEntity>>
    ): String {
        val actId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        intermediateEstimateActDao.insertAct(
            IntermediateEstimateActEntity(id = actId, projectId = projectId, title = title, createdAt = now)
        )
        val actItems = items.mapIndexed { index, (roomName, item) ->
            IntermediateEstimateActItemEntity(
                id = UUID.randomUUID().toString(),
                actId = actId,
                workItemId = item.id,
                roomName = roomName,
                category = item.category,
                name = item.name,
                unitAbbr = item.unitAbbr,
                price = item.price,
                quantity = item.quantity,
                sortOrder = index
            )
        }
        intermediateEstimateActDao.insertActItems(actItems)
        return actId
    }

    suspend fun getIntermediateEstimateActsSync(projectId: String): List<IntermediateEstimateActEntity> =
        intermediateEstimateActDao.getActsByProjectIdSync(projectId)

    suspend fun getIntermediateEstimateActItems(actId: String): List<IntermediateEstimateActItemEntity> =
        intermediateEstimateActDao.getItemsByActId(actId)

    suspend fun updateIntermediateEstimateActItem(item: IntermediateEstimateActItemEntity) {
        intermediateEstimateActDao.updateActItem(
            itemId = item.id,
            roomName = item.roomName,
            category = item.category,
            name = item.name,
            unitAbbr = item.unitAbbr,
            price = item.price,
            quantity = item.quantity
        )
    }

    /** Id видов работ, уже включённых в какой-либо акт по проекту. */
    suspend fun getWorkItemIdsAlreadyInActsSync(projectId: String): Set<String> =
        intermediateEstimateActDao.getWorkItemIdsInActsSync(projectId).toSet()

    /** Удаляет промежуточную смету (акт) и все её позиции. После удаления виды работ снова доступны в предварительной смете. */
    suspend fun deleteIntermediateEstimateAct(actId: String) {
        intermediateEstimateActDao.deleteActWithItems(actId)
    }

    suspend fun updateProject(projectId: String, data: ProjectFormData) {
        val existing = projectDao.getProjectById(projectId) ?: return
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            lastName = data.lastName,
            firstName = data.firstName,
            middleName = data.middleName,
            email = data.email,
            phone = data.phone,
            address = data.address,
            city = data.city,
            street = data.street,
            house = data.house,
            apartment = data.apartment,
            updatedAt = now
        )
        projectDao.insertProject(updated)
    }

    /** Обновляет скидку и процент ИП для проекта (раздел предварительная смета). */
    suspend fun updateProjectDiscountTax(projectId: String, discountText: String?, taxPercentText: String?) {
        val existing = projectDao.getProjectById(projectId) ?: return
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            discountText = discountText?.takeIf { it.isNotBlank() },
            taxPercentText = taxPercentText?.takeIf { it.isNotBlank() },
            updatedAt = now
        )
        projectDao.insertProject(updated)
    }

    /**
     * Создаёт один демонстрационный проект с несколькими комнатами,
     * если в базе ещё нет ни одного проекта.
     */
    suspend fun ensureDemoProject() {
        val count = projectDao.getProjectsCount()
        if (count > 0) return

        val demoProjectData = ProjectFormData(
            lastName = "Демо",
            firstName = "Проект",
            middleName = null,
            email = "",
            phone = "",
            address = "г. Краснодар, ул. Примерная, д. 1",
            city = "Краснодар",
            street = "Примерная",
            house = "1",
            apartment = "1"
        )
        val livingRoom = RoomFormData(
            name = "Гостиная",
            length = 5.0,
            width = 4.0,
            height = 2.8,
            hasSlopes = false,
            slopesLength = 0.0,
            hasBoxes = false,
            boxesLength = 0.0
        )
        val demoProjectId = addProject(demoProjectData, livingRoom)

        val kitchen = RoomFormData(
            name = "Кухня",
            length = 3.5,
            width = 3.0,
            height = 2.8,
            hasSlopes = true,
            slopesLength = 4.0,
            hasBoxes = false,
            boxesLength = 0.0
        )
        val bathroom = RoomFormData(
            name = "Санузел",
            length = 2.0,
            width = 1.7,
            height = 2.5,
            hasSlopes = false,
            slopesLength = 0.0,
            hasBoxes = true,
            boxesLength = 3.0
        )

        addRoom(demoProjectId, kitchen, emptyList())
        addRoom(demoProjectId, bathroom, emptyList())
    }

    private fun createRoomEntity(
        form: RoomFormData,
        projectId: String,
        roomId: String = UUID.randomUUID().toString(),
        openings: List<OpeningFormData> = emptyList()
    ): RoomEntity {
        val now = System.currentTimeMillis()
        val floorArea = form.floorAreaOverride ?: (form.length * form.width)
        val ceilingArea = form.ceilingAreaOverride ?: (form.length * form.width)
        val baseWallArea = 2 * (form.length + form.width) * form.height
        val openingsArea = openings.sumOf { it.width * it.height * it.count }
        val wallArea = form.wallAreaOverride ?: (baseWallArea - openingsArea).coerceAtLeast(0.0)
        return RoomEntity(
            id = roomId,
            projectId = projectId,
            name = form.name,
            length = form.length,
            width = form.width,
            height = form.height,
            floorArea = floorArea,
            wallArea = wallArea,
            ceilingArea = ceilingArea,
            perimeter = 2 * (form.length + form.width),
            hasSlopes = form.hasSlopes,
            slopesLength = form.slopesLength,
            hasBoxes = form.hasBoxes,
            boxesLength = form.boxesLength,
            createdAt = now,
            updatedAt = now
        )
    }
}
