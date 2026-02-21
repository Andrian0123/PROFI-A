package ru.profia.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intermediate_estimate_act_items",
    foreignKeys = [
        ForeignKey(
            entity = IntermediateEstimateActEntity::class,
            parentColumns = ["id"],
            childColumns = ["actId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("actId")]
)
data class IntermediateEstimateActItemEntity(
    @PrimaryKey
    val id: String,
    val actId: String,
    /** id вида работы из room_work_items, если позиция взята из сметы (для исключения из выбора при новой промежуточной смете). */
    val workItemId: String? = null,
    val roomName: String,
    val category: String,
    val name: String,
    val unitAbbr: String,
    val price: Double,
    val quantity: Double,
    val sortOrder: Int
) {
    val total: Double get() = price * quantity
}
