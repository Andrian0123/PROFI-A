package ru.profia.app.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Юнит-тесты для позиции акта (расчёт итога по позиции).
 */
class IntermediateEstimateActItemEntityTest {

    @Test
    fun total_isPriceTimesQuantity() {
        val item = IntermediateEstimateActItemEntity(
            id = "1",
            actId = "act1",
            workItemId = "work1",
            roomName = "Кухня",
            category = "Потолок",
            name = "Штукатурка",
            unitAbbr = "кв.м.",
            price = 500.0,
            quantity = 10.0,
            sortOrder = 0
        )
        assertEquals(5000.0, item.total, 0.001)
    }

    @Test
    fun total_zeroQuantity_returnsZero() {
        val item = IntermediateEstimateActItemEntity(
            id = "2",
            actId = "act1",
            roomName = "Комната",
            category = "Стены",
            name = "Обои",
            unitAbbr = "кв.м.",
            price = 300.0,
            quantity = 0.0,
            sortOrder = 1
        )
        assertEquals(0.0, item.total, 0.001)
    }

    @Test
    fun actTotalSum_isSumOfItemTotals() {
        val items = listOf(
            IntermediateEstimateActItemEntity("1", "act1", null, "Кухня", "Потолок", "Штукатурка", "кв.м.", 500.0, 10.0, 0),
            IntermediateEstimateActItemEntity("2", "act1", null, "Кухня", "Стены", "Обои", "кв.м.", 300.0, 20.0, 1),
            IntermediateEstimateActItemEntity("3", "act1", null, "Коридор", "Пол", "Ламинат", "кв.м.", 800.0, 5.0, 2)
        )
        val totalSum = items.sumOf { it.total }
        assertEquals(5000.0 + 6000.0 + 4000.0, totalSum, 0.001)
    }
}
