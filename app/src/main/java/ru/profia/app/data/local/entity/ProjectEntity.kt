package ru.profia.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val email: String?,
    val phone: String?,
    val address: String,
    val city: String?,
    val street: String?,
    val house: String?,
    val apartment: String?,
    val discountText: String? = null,
    val taxPercentText: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
