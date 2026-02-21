package ru.profia.app.data.model

/**
 * Данные комнаты с расчётами площадей.
 */
data class RoomData(
    val id: String,
    val name: String,
    val length: Double,
    val width: Double,
    val height: Double,
    val floorArea: Double,
    val wallArea: Double,
    val ceilingArea: Double,
    val perimeter: Double,
    val hasSlopes: Boolean = false,
    val slopesLength: Double = 0.0,
    val hasBoxes: Boolean = false,
    val boxesLength: Double = 0.0,
    val openings: List<OpeningData> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Данные проёма (окно или дверь).
 */
data class OpeningData(
    val id: String,
    val type: OpeningType,
    val width: Double,
    val height: Double,
    val count: Int
)

enum class OpeningType {
    DOOR,
    WINDOW
}
