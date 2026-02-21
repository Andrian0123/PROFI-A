package ru.profia.app.data.model

import java.util.Date
import java.util.UUID

/** Категория работ для КС-2 */
enum class WorkCategory {
    CONSTRUCTION,
    INSTALLATION,
    OTHER
}

/** Информация о контрагенте (заказчик, подрядчик и т.д.) */
data class ContractorInfo(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val ogrn: String = "",
    val inn: String = "",
    val kpp: String = "",
    val okpo: String = "",
    val directorName: String = ""
)

/** Информация об объекте строительства */
data class ConstructionInfo(
    val name: String = "",
    val address: String = ""
)

/** Позиция акта КС-2 (вид выполненных работ) */
data class KS2Item(
    val id: String = UUID.randomUUID().toString(),
    val positionNumber: Int = 0,
    val estimatePosition: String = "",
    val workName: String = "",
    val unitPriceCode: String = "",
    val unit: String = "",
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val overheads: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val workCategory: WorkCategory = WorkCategory.OTHER,
    val roomId: String? = null
)

/**
 * Акт о приёмке выполненных работ (форма КС-2).
 */
data class KS2Act(
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
    val contractor: ContractorInfo = ContractorInfo(),
    val subcontractor: ContractorInfo? = null,
    val construction: ConstructionInfo = ConstructionInfo(),
    val objectName: String = "",
    val items: MutableList<KS2Item> = mutableListOf(),
    val totalAmount: Double = 0.0,
    val vatRate: Double = 20.0,
    val vatAmount: Double = 0.0,
    val totalWithVat: Double = 0.0,
    val signedByContractor: Boolean = false,
    val signedByCustomer: Boolean = false
)
