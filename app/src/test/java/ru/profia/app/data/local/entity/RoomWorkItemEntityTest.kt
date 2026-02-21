package ru.profia.app.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Юнит-тесты для позиции вида работ в комнате (расчёт итога по позиции).
 */
class RoomWorkItemEntityTest {

    @Test
    fun total_isPriceTimesQuantity() {
        val item = RoomWorkItemEntity(
            id = "1",
            roomId = "room1",
            category = "Потолок",
            name = "Штукатурка",
            unitAbbr = "кв.м.",
            price = 500.0,
            quantity = 12.5
        )
        assertEquals(6250.0, item.total, 0.001)
    }

    @Test
    fun total_zeroQuantity_returnsZero() {
        val item = RoomWorkItemEntity(
            id = "2",
            roomId = "room1",
            category = "Стены",
            name = "Обои",
            unitAbbr = "кв.м.",
            price = 300.0,
            quantity = 0.0
        )
        assertEquals(0.0, item.total, 0.001)
    }

    @Test
    fun roomTotalSum_isSumOfItemTotals() {
        val items = listOf(
            RoomWorkItemEntity("1", "room1", "Потолок", "Штукатурка", "кв.м.", 500.0, 10.0),
            RoomWorkItemEntity("2", "room1", "Стены", "Обои", "кв.м.", 300.0, 20.0),
            RoomWorkItemEntity("3", "room1", "Пол", "Ламинат", "кв.м.", 800.0, 5.0)
        )
        val totalSum = items.sumOf { it.total }
        assertEquals(5000.0 + 6000.0 + 4000.0, totalSum, 0.001)
    }
}
