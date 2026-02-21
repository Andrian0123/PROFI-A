package ru.profia.app.ui.export

/**
 * Утилиты для экспорта в CSV (экран сметы и актов).
 * Вынесены для возможности юнит-тестирования.
 */
object ExportUtils {

    /**
     * Экранирует строку для CSV: если есть `;`, `"`, `\n`, `\r` или `\t` — оборачивает в кавычки и удваивает кавычки внутри.
     */
    fun escapeCsv(s: String): String =
        if (s.contains(';') || s.contains('"') || s.contains('\n') || s.contains('\r') || s.contains('\t')) "\"${s.replace("\"", "\"\"")}\"" else s
}
