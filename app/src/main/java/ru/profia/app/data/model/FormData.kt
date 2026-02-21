package ru.profia.app.data.model

import java.io.Serializable
import kotlin.text.RegexOption

data class ProjectFormData(
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val email: String?,
    val phone: String?,
    val address: String,
    val city: String?,
    val street: String?,
    val house: String?,
    val apartment: String?
)

data class OpeningFormData(
    val type: OpeningType,
    val width: Double,
    val height: Double,
    val count: Int
)

data class RoomFormData(
    val name: String,
    val length: Double,
    val width: Double,
    val height: Double,
    val floorAreaOverride: Double? = null,
    val ceilingAreaOverride: Double? = null,
    val wallAreaOverride: Double? = null,
    val hasSlopes: Boolean,
    val slopesLength: Double,
    val hasBoxes: Boolean,
    val boxesLength: Double
) : Serializable

/** Подсказка «добавить этаж с теми же параметрами» после сохранения комнаты с названием «этаж». */
data class SuggestAddFloorData(
    val nextFloorName: String,
    val formData: RoomFormData
) : Serializable

/** Один вид работы в комнате (для сохранения в БД и общей сметы). */
data class RoomWorkItemForm(
    val category: String,
    val name: String,
    val unitAbbr: String,
    val price: Double,
    val quantity: Double
)

/**
 * Подсказка: вид работы, использованный в других комнатах проекта.
 * Для быстрого формирования предварительной сметы (добавить одним нажатием).
 */
data class SuggestedWorkItem(
    val category: String,
    val name: String,
    val unitAbbr: String,
    val price: Double,
    /** В скольких комнатах проекта уже есть этот вид работы. */
    val roomCount: Int
)

/** Если название комнаты содержит «этаж», возвращает название следующего этажа (1 этаж → 2 этаж и т.д.). */
fun nextFloorNameIfRelevant(currentRoomName: String): String? {
    if (!currentRoomName.contains("этаж", ignoreCase = true)) return null
    val regex = Regex("""(\d+)\s*этаж""", RegexOption.IGNORE_CASE)
    val match = regex.find(currentRoomName.trim())
    val nextNum = if (match != null) (match.groupValues[1].toIntOrNull() ?: 1) + 1 else 2
    return "$nextNum этаж"
}
