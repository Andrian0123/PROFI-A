package ru.profia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.profia.app.data.local.entity.RoomWorkItemEntity

@Dao
interface RoomWorkItemDao {
    @Query("SELECT * FROM room_work_items WHERE roomId = :roomId ORDER BY category, name")
    fun getByRoomId(roomId: String): Flow<List<RoomWorkItemEntity>>

    @Query("SELECT * FROM room_work_items WHERE roomId = :roomId ORDER BY category, name")
    suspend fun getByRoomIdSync(roomId: String): List<RoomWorkItemEntity>

    @Query("SELECT w.* FROM room_work_items w INNER JOIN rooms r ON w.roomId = r.id WHERE r.projectId = :projectId ORDER BY r.name, w.category, w.name")
    suspend fun getByProjectIdSync(projectId: String): List<RoomWorkItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RoomWorkItemEntity>)

    @Query("DELETE FROM room_work_items WHERE roomId = :roomId")
    suspend fun deleteByRoomId(roomId: String)

    @Query("DELETE FROM room_work_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RoomWorkItemEntity)
}
