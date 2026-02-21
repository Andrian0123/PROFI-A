package ru.profia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.profia.app.data.local.entity.RoomEntity

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE projectId = :projectId")
    fun getRoomsByProjectId(projectId: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE projectId = :projectId")
    suspend fun getRoomsByProjectIdSync(projectId: String): List<RoomEntity>

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomById(roomId: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: String)
}
