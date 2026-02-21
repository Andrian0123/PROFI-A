package ru.profia.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class RoomEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val name: String,
    val length: Double,
    val width: Double,
    val height: Double,
    val floorArea: Double,
    val wallArea: Double,
    val ceilingArea: Double,
    val perimeter: Double,
    val hasSlopes: Boolean,
    val slopesLength: Double,
    val hasBoxes: Boolean,
    val boxesLength: Double,
    val createdAt: Long,
    val updatedAt: Long
)
