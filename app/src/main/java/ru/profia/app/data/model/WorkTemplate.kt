package ru.profia.app.data.model

/**
 * Шаблон вида работы (сохраняется пользователем на экране «Виды работ»).
 * Используется при добавлении работ в комнату для подстановки названия, категории, единицы и цены.
 */
data class WorkTemplate(
    val id: String,
    val name: String,
    val category: String,
    val unitAbbr: String,
    val defaultPrice: Double
)
