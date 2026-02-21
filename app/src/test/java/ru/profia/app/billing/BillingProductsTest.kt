package ru.profia.app.billing

import org.junit.Assert.assertEquals
import org.junit.Test

class BillingProductsTest {

    @Test
    fun productIdForMonths_mapsCorrectly() {
        assertEquals(BillingProducts.SUB_1_MONTH, BillingProducts.productIdForMonths(1))
        assertEquals(BillingProducts.SUB_6_MONTHS, BillingProducts.productIdForMonths(6))
        assertEquals(BillingProducts.SUB_12_MONTHS, BillingProducts.productIdForMonths(12))
        assertEquals(BillingProducts.SUB_1_MONTH, BillingProducts.productIdForMonths(3))
    }
}
