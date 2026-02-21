package ru.profia.app.data.model

/**
 * Полные данные проекта.
 */
data class ProjectData(
    val id: String,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String,
    val city: String? = null,
    val street: String? = null,
    val house: String? = null,
    val apartment: String? = null,
    val rooms: MutableList<RoomData> = mutableListOf(),
    val createdAt: Long,
    val updatedAt: Long
) {
    /** Отображаемое имя проекта (ФИО или адрес). */
    val displayName: String
        get() = listOf(lastName, firstName, middleName ?: "").joinToString(" ").trim()
            .ifBlank { address }
}
