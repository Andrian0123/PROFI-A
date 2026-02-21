package ru.profia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity

@Dao
interface IntermediateEstimateActDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAct(act: IntermediateEstimateActEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActItems(items: List<IntermediateEstimateActItemEntity>)

    @Query("UPDATE intermediate_estimate_act_items SET roomName = :roomName, category = :category, name = :name, unitAbbr = :unitAbbr, price = :price, quantity = :quantity WHERE id = :itemId")
    suspend fun updateActItem(itemId: String, roomName: String, category: String, name: String, unitAbbr: String, price: Double, quantity: Double)

    @Query("SELECT * FROM intermediate_estimate_acts WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getActsByProjectId(projectId: String): Flow<List<IntermediateEstimateActEntity>>

    @Query("SELECT * FROM intermediate_estimate_acts WHERE projectId = :projectId ORDER BY createdAt DESC")
    suspend fun getActsByProjectIdSync(projectId: String): List<IntermediateEstimateActEntity>

    @Query("SELECT * FROM intermediate_estimate_act_items WHERE actId = :actId ORDER BY sortOrder")
    suspend fun getItemsByActId(actId: String): List<IntermediateEstimateActItemEntity>

    @Query("DELETE FROM intermediate_estimate_act_items WHERE actId = :actId")
    suspend fun deleteActItemsByActId(actId: String)

    @Query("DELETE FROM intermediate_estimate_acts WHERE id = :actId")
    suspend fun deleteAct(actId: String)

    @Transaction
    suspend fun deleteActWithItems(actId: String) {
        deleteActItemsByActId(actId)
        deleteAct(actId)
    }

    /** Id видов работ, уже включённых в какой-либо акт по проекту (для исключения из выбора при создании новой промежуточной сметы). */
    @Query("SELECT workItemId FROM intermediate_estimate_act_items WHERE actId IN (SELECT id FROM intermediate_estimate_acts WHERE projectId = :projectId) AND workItemId IS NOT NULL")
    suspend fun getWorkItemIdsInActsSync(projectId: String): List<String>
}
