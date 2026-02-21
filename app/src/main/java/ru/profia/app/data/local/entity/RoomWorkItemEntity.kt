package ru.profia.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_work_items",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roomId")]
)
data class RoomWorkItemEntity(
    @PrimaryKey
    val id: String,
    val roomId: String,
    /** Раздел: Потолок, Стены, Пол, Двери, Окна, Сантехника, Электрика, Вентиляция, Прочие работы */
    val category: String,
    val name: String,
    val unitAbbr: String,
    val price: Double,
    val quantity: Double
) {
    val total: Double get() = price * quantity
}
