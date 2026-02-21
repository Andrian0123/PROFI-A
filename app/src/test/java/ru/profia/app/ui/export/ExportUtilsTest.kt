package ru.profia.app.ui.export

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Юнит-тесты для логики экспорта в CSV (экран сметы и актов).
 */
class ExportUtilsTest {

    @Test
    fun escapeCsv_plainString_unchanged() {
        assertEquals("Комната", ExportUtils.escapeCsv("Комната"))
        assertEquals("Штукатурка", ExportUtils.escapeCsv("Штукатурка"))
        assertEquals("кв.м.", ExportUtils.escapeCsv("кв.м."))
        assertEquals("", ExportUtils.escapeCsv(""))
    }

    @Test
    fun escapeCsv_containsSemicolon_wrappedInQuotes() {
        assertEquals("\"Помещение; кабинет\"", ExportUtils.escapeCsv("Помещение; кабинет"))
        assertEquals("\"a;b;c\"", ExportUtils.escapeCsv("a;b;c"))
    }

    @Test
    fun escapeCsv_containsQuote_escapedAndWrapped() {
        assertEquals("\"\"\"цитата\"\"\"", ExportUtils.escapeCsv("\"цитата\""))
        assertEquals("\"a\"\"b\"", ExportUtils.escapeCsv("a\"b"))
    }

    @Test
    fun escapeCsv_containsNewline_wrappedInQuotes() {
        assertEquals("\"строка1\nстрока2\"", ExportUtils.escapeCsv("строка1\nстрока2"))
    }

    @Test
    fun escapeCsv_multipleSpecialChars_wrappedAndEscaped() {
        assertEquals("\"a;b\"\"c\nd\"", ExportUtils.escapeCsv("a;b\"c\nd"))
    }

    @Test
    fun escapeCsv_containsCarriageReturn_wrappedInQuotes() {
        assertEquals("\"строка1\rстрока2\"", ExportUtils.escapeCsv("строка1\rстрока2"))
    }

    @Test
    fun escapeCsv_containsTab_wrappedInQuotes() {
        assertEquals("\"a\tb\"", ExportUtils.escapeCsv("a\tb"))
    }
}
