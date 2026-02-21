package ru.profia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.profia.app.data.local.entity.OpeningEntity

@Dao
interface OpeningDao {
    @Query("SELECT * FROM openings WHERE roomId = :roomId")
    suspend fun getOpeningsByRoomId(roomId: String): List<OpeningEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpening(opening: OpeningEntity)

    @Query("DELETE FROM openings WHERE roomId = :roomId")
    suspend fun deleteOpeningsByRoomId(roomId: String)
}
