package ru.profia.app.data.model

/**
 * Модель проекта для отображения в списке.
 */
data class Project(
    val id: String,
    val name: String,
    val address: String,
    val date: String,
    val cost: String,
    val roomCount: Int,
    val isEditable: Boolean = false
)
