package ru.profia.app.data.repository

import kotlinx.coroutines.flow.Flow
import ru.profia.app.data.local.dao.RoomScanDao
import ru.profia.app.data.local.entity.RoomScanEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomScanRepository @Inject constructor(
    private val roomScanDao: RoomScanDao
) {
    fun getScansByProject(projectId: String): Flow<List<RoomScanEntity>> =
        roomScanDao.getScansByProjectId(projectId)

    fun getScansByRoom(projectId: String, roomId: String): Flow<List<RoomScanEntity>> =
        roomScanDao.getScansByRoom(projectId, roomId)

    suspend fun getScanById(scanId: String): RoomScanEntity? =
        roomScanDao.getScanById(scanId)

    /** Создать новый скан (при открытии экрана «Фото 3D» → Создать). */
    suspend fun createScan(projectId: String, roomId: String): RoomScanEntity {
        val now = System.currentTimeMillis()
        val scan = RoomScanEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            roomId = roomId,
            createdAt = now,
            updatedAt = now,
            status = "draft"
        )
        roomScanDao.insertScan(scan)
        return scan
    }

    suspend fun updateScan(scan: RoomScanEntity) {
        roomScanDao.updateScan(scan.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun markScanReady(scanId: String) {
        roomScanDao.getScanById(scanId)?.let { scan ->
            roomScanDao.updateScan(scan.copy(status = "ready", updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteScan(scanId: String) = roomScanDao.deleteScan(scanId)

    /** Сохраняет путь к загруженной 3D-модели для комнаты (создаёт скан при необходимости). */
    suspend fun setMeshPathForRoom(projectId: String, roomId: String, meshPath: String) {
        val existing = roomScanDao.getScansByRoomSync(projectId, roomId).firstOrNull()
        val now = System.currentTimeMillis()
        if (existing != null) {
            roomScanDao.updateScan(existing.copy(meshPath = meshPath, updatedAt = now))
        } else {
            val scan = RoomScanEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                roomId = roomId,
                createdAt = now,
                updatedAt = now,
                meshPath = meshPath,
                status = "draft"
            )
            roomScanDao.insertScan(scan)
        }
    }
}
