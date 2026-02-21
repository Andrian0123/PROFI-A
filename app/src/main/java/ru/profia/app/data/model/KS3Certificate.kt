package ru.profia.app.data.model

import java.util.Date
import java.util.UUID

/** Строка справки КС-3 (нарастающие итоги) */
data class KS3Item(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val costFromStart: Double = 0.0,
    val costFromYearStart: Double = 0.0,
    val costForPeriod: Double = 0.0,
    val ks2Ids: List<String> = emptyList()
)

/**
 * Справка о стоимости выполненных работ и затрат (форма КС-3).
 */
data class KS3Certificate(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val number: String = "",
    val date: Date = Date(),
    val reportPeriodStart: Date = Date(),
    val reportPeriodEnd: Date = Date(),
    val contractNumber: String = "",
    val contractDate: Date = Date(),
    val investor: ContractorInfo = ContractorInfo(),
    val customer: ContractorInfo = ContractorInfo(),
    val generalContractor: ContractorInfo = ContractorInfo(),
    val subcontractor: ContractorInfo? = null,
    val construction: ConstructionInfo = ConstructionInfo(),
    val items: MutableList<KS3Item> = mutableListOf(),
    val totalWorksCost: Double = 0.0,
    val vatRate: Double = 20.0,
    val vatAmount: Double = 0.0,
    val totalWithVat: Double = 0.0,
    val cumulativeFromStart: Double = 0.0,
    val cumulativeFromYearStart: Double = 0.0
)
