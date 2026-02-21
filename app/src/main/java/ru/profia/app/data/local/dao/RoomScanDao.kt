package ru.profia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.profia.app.data.local.entity.RoomScanEntity

@Dao
interface RoomScanDao {
    @Query("SELECT * FROM room_scans WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getScansByProjectId(projectId: String): Flow<List<RoomScanEntity>>

    @Query("SELECT * FROM room_scans WHERE projectId = :projectId AND roomId = :roomId ORDER BY updatedAt DESC")
    fun getScansByRoom(projectId: String, roomId: String): Flow<List<RoomScanEntity>>

    @Query("SELECT * FROM room_scans WHERE projectId = :projectId AND roomId = :roomId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getScansByRoomSync(projectId: String, roomId: String): List<RoomScanEntity>

    @Query("SELECT * FROM room_scans WHERE id = :scanId")
    suspend fun getScanById(scanId: String): RoomScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: RoomScanEntity)

    @Update
    suspend fun updateScan(scan: RoomScanEntity)

    @Query("DELETE FROM room_scans WHERE id = :scanId")
    suspend fun deleteScan(scanId: String)
}
