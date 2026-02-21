package ru.profia.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "openings",
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
data class OpeningEntity(
    @PrimaryKey
    val id: String,
    val roomId: String,
    val type: String, // "DOOR" or "WINDOW"
    val width: Double,
    val height: Double,
    val count: Int
)
