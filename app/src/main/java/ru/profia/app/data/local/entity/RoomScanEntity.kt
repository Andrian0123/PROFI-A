package ru.profia.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Запись 3D-скана помещения (по аналогии с Polycam).
 * Связь с проектом и комнатой; пути к превью и мешу — для будущего экспорта.
 */
@Entity(
    tableName = "room_scans",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("roomId")]
)
data class RoomScanEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    /** id комнаты или "new" если комната ещё не создана */
    val roomId: String,
    val createdAt: Long,
    val updatedAt: Long,
    /** Путь к превью-изображению (для списка сканов) */
    val thumbnailPath: String? = null,
    /** Путь к 3D-модели/мешу (на будущее) */
    val meshPath: String? = null,
    /** "draft" — в процессе, "ready" — сохранён */
    val status: String = "draft",
    val title: String? = null
)
