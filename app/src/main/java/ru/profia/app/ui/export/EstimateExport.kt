package ru.profia.app.ui.export

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.ui.viewmodel.EstimateRoomSection
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PDF_PAGE_WIDTH = 595
private const val PDF_PAGE_HEIGHT = 842
private const val MARGIN = 40
private const val LINE_HEIGHT = 18
private const val ROW_HEIGHT = 14f
private const val TITLE_SIZE = 16f
private const val BODY_SIZE = 10f

// Колонки таблицы работ: левые границы и правые (для выравнивания по правому краю)
private const val TABLE_LEFT = 40f
private const val TABLE_RIGHT = 555f     // PDF_PAGE_WIDTH - MARGIN
private const val COL_NO_X = 40f
private const val COL_NO_RIGHT = 56f     // №
private const val COL_NAME_X = 60f       // Наименование работ (не выходить за COL_NAME_RIGHT)
private const val COL_NAME_RIGHT = 268f  // конец колонки «Наименование», до Ед.изм.
private const val COL_NAME_W = 208f      // ширина для обрезки текста (COL_NAME_RIGHT - COL_NAME_X)
private const val COL_UNIT_X = 272f      // Ед. изм.
private const val COL_UNIT_RIGHT = 308f
private const val COL_QTY_RIGHT = 348f   // Кол-во (правый край)
private const val COL_PRICE_RIGHT = 438f // Цена (правый край)
private const val COL_SUM_RIGHT = 555f   // Сумма (правый край = TABLE_RIGHT)
private const val GRID_COLOR = 0xFFE0E0E0.toInt()
private const val BLOCK_BG_COLOR = 0xFFF5F5F5.toInt()

/** Извлекает числовое значение процента из строки вида "20%" или "20". */
private fun parsePercentFromTaxText(taxPercentText: String?): Double? = parsePercent(discountOrTaxText = taxPercentText)

/** То же для скидки (например "5%", "10%"). */
private fun parsePercentFromDiscountText(discountText: String?): Double? = parsePercent(discountOrTaxText = discountText)

private fun parsePercent(discountOrTaxText: String?): Double? = discountOrTaxText?.let { text ->
    // Извлекаем число из строк вида "5%", "10%", "НДС 20%" и т.п.
    val numStr = """(\d+([.,]\d+)?)""".toRegex().find(text)?.groupValues?.get(1)?.replace(",", ".")
    numStr?.toDoubleOrNull()
}?.takeIf { it >= 0 && it <= 100 }

/** Строка «Прибыль базовая» не выводится в экспорт акта (скрыта по запросу). Проверка на всех языках: RU, EN, DE, RO. */
private fun isBaseProfitRow(name: String): Boolean {
    val n = name.lowercase()
    return n.contains("прибыль базовая") ||   // RU
        n.contains("base profit") ||           // EN
        n.contains("grundgewinn") ||           // DE
        n.contains("basisgewinn") ||           // DE (альт.)
        n.contains("profit de bază") ||        // RO
        n.contains("profit bază") ||          // RO кратко
        n.contains("profit de baza") ||       // RO без диакритики
        n.contains("profit baza")              // RO кратко без диакритики
}

/** Переводит название категории работ на язык контекста (для экспорта акта на выбранном языке). */
private fun translateWorkCategory(ctx: Context, category: String): String = when (category) {
    "Потолок" -> ctx.getString(ru.profia.app.R.string.work_category_ceiling)
    "Стены" -> ctx.getString(ru.profia.app.R.string.work_category_walls)
    "Пол" -> ctx.getString(ru.profia.app.R.string.work_category_floor)
    "Двери" -> ctx.getString(ru.profia.app.R.string.work_category_doors)
    "Окна" -> ctx.getString(ru.profia.app.R.string.work_category_windows)
    "Сантехника" -> ctx.getString(ru.profia.app.R.string.work_category_plumbing)
    "Электрика" -> ctx.getString(ru.profia.app.R.string.work_category_electrical)
    "Вентиляция" -> ctx.getString(ru.profia.app.R.string.work_category_ventilation)
    "Прочие работы" -> ctx.getString(ru.profia.app.R.string.work_category_other)
    else -> category
}

/**
 * Экспорт сметы в файл для шаринга.
 * PDF — через [exportToPdf], Excel-совместимый CSV — через [exportToCsv].
 */
object EstimateExport {

    fun exportToPdf(
        context: Context,
        sections: List<EstimateRoomSection>,
        @Suppress("UNUSED_PARAMETER") totalSum: Double,
        title: String = "Предварительная смета",
        filePrefix: String = "smeta",
        fileName: String = "${filePrefix}_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf",
        customerLines: List<String>? = null,
        executorLines: List<String>? = null,
        exportLocale: Locale = Locale.getDefault(),
        titleResId: Int? = null
    ): File? {
        val config = Configuration(context.resources.configuration).apply { setLocale(exportLocale) }
        val ctx = context.createConfigurationContext(config)
        return try {
            val file = File(context.cacheDir, fileName)
            val doc = PdfDocument()
            val sansSerif = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val sansSerifBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            val paintTitle = Paint().apply {
                textSize = TITLE_SIZE
                setTypeface(sansSerifBold)
            }
            val paintSmall = Paint().apply { textSize = 9f; setTypeface(sansSerif); color = Color.DKGRAY }
            val paintGrid = Paint().apply {
                color = GRID_COLOR
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
                isAntiAlias = true
            }
            val paintBlockBg = Paint().apply { color = BLOCK_BG_COLOR; style = Paint.Style.FILL }
            var page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
            var y = MARGIN.toFloat()

            fun drawText(canvas: android.graphics.Canvas, text: String, paint: Paint) {
                canvas.drawText(text, MARGIN.toFloat(), y, paint)
                y += LINE_HEIGHT
            }

            fun drawTextCentered(canvas: android.graphics.Canvas, text: String, paint: Paint) {
                val x = (PDF_PAGE_WIDTH / 2 - paint.measureText(text) / 2).toFloat()
                canvas.drawText(text, x, y, paint)
                y += LINE_HEIGHT
            }

            fun maybeNewPage() {
                if (y > (PDF_PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 3)) {
                    doc.finishPage(page)
                    page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
                    y = MARGIN.toFloat()
                }
            }

            val displayTitle = if (titleResId != null) ctx.getString(titleResId) else title
            drawTextCentered(page.canvas, displayTitle, paintTitle)
            y += LINE_HEIGHT

            // Слева — данные заказчика, справа — исполнителя; слегка выделяем фоном
            val rightColumnX = PDF_PAGE_WIDTH - MARGIN
            val midX = PDF_PAGE_WIDTH / 2f
            if (!customerLines.isNullOrEmpty() || !executorLines.isNullOrEmpty()) {
                val blockTop = y
                val maxLines = maxOf(customerLines?.size ?: 0, executorLines?.size ?: 0)
                val lineStep = (LINE_HEIGHT * 0.9f).toInt().toFloat()
                val blockBottom = blockTop + maxLines * lineStep + 4f
                page.canvas.drawRect(RectF(MARGIN.toFloat(), blockTop - 2, midX - 8, blockBottom), paintBlockBg)
                page.canvas.drawRect(RectF(midX + 8, blockTop - 2, rightColumnX.toFloat(), blockBottom), paintBlockBg)
                for (i in 0 until maxLines) {
                    val leftText = customerLines?.getOrNull(i) ?: ""
                    val rightText = executorLines?.getOrNull(i) ?: ""
                    if (leftText.isNotEmpty() || rightText.isNotEmpty()) {
                        page.canvas.drawText(leftText, MARGIN.toFloat(), y, paintSmall)
                        if (rightText.isNotEmpty()) {
                            page.canvas.drawText(rightText, rightColumnX - paintSmall.measureText(rightText), y, paintSmall)
                        }
                        y += lineStep
                    }
                }
                y = blockBottom + LINE_HEIGHT
            }
            val paintTable = Paint().apply { textSize = 9f; setTypeface(sansSerif) }
            val paintTableBold = Paint().apply { textSize = 9f; setTypeface(sansSerifBold) }
            val paintRoomName = Paint().apply { textSize = 9f; setTypeface(sansSerif); color = Color.GRAY }
            val fm = Paint.FontMetrics()
            fun baselineForRow(rowTop: Float, paint: Paint): Float {
                paint.getFontMetrics(fm)
                return rowTop + ROW_HEIGHT - fm.descent
            }
            val colXList = floatArrayOf(TABLE_LEFT, COL_NO_RIGHT, COL_NAME_X, COL_NAME_RIGHT, COL_UNIT_X, COL_UNIT_RIGHT, COL_QTY_RIGHT, COL_PRICE_RIGHT, COL_SUM_RIGHT, TABLE_RIGHT)
            fun drawVerticalGrid(fromY: Float, toY: Float) {
                for (x in colXList) page.canvas.drawLine(x, fromY, x, toY, paintGrid)
            }
            fun drawTableHeader() {
                val headerY = y
                val headerBaseline = baselineForRow(headerY, paintTableBold)
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_no), COL_NO_X, headerBaseline, paintTableBold)
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_work_name), COL_NAME_X, headerBaseline, paintTableBold)
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_unit), COL_UNIT_X, headerBaseline, paintTableBold)
                paintTable.textAlign = Paint.Align.RIGHT
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_qty), COL_QTY_RIGHT, headerBaseline, paintTableBold)
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_price), COL_PRICE_RIGHT, headerBaseline, paintTableBold)
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_sum), COL_SUM_RIGHT, headerBaseline, paintTableBold)
                paintTable.textAlign = Paint.Align.LEFT
                y += ROW_HEIGHT + 4
                page.canvas.drawLine(TABLE_LEFT, headerY - 10f, TABLE_RIGHT, headerY - 10f, paintGrid)
                page.canvas.drawLine(TABLE_LEFT, y, TABLE_RIGHT, y, paintGrid)
                drawVerticalGrid(headerY - 10f, y)
            }
            fun truncateName(text: String, p: Paint): String {
                if (p.measureText(text) <= COL_NAME_W) return text
                var s = text
                while (s.length > 1 && p.measureText("$s…") > COL_NAME_W) s = s.dropLast(1)
                return if (s.length < text.length) "$s…" else text
            }
            var rowNum = 0
            drawTableHeader()
            for (section in sections) {
                maybeNewPage()
                val roomRowTop = y
                page.canvas.drawLine(TABLE_LEFT, roomRowTop, TABLE_RIGHT, roomRowTop, paintGrid)
                y += ROW_HEIGHT
                val roomRowBottom = y
                paintRoomName.getFontMetrics(fm)
                val roomBaseline = roomRowBottom - fm.descent
                val roomNameShort = truncateName(section.roomName, paintRoomName)
                page.canvas.drawText(roomNameShort, COL_NAME_X, roomBaseline, paintRoomName)
                page.canvas.drawLine(TABLE_LEFT, roomRowBottom, TABLE_RIGHT, roomRowBottom, paintGrid)
                drawVerticalGrid(roomRowTop, roomRowBottom)
                for (ui in section.items) {
                    val item = ui.item
                    maybeNewPage()
                    rowNum++
                    val rowTop = y
                    val rowBaseline = baselineForRow(rowTop, paintTable)
                    val catTranslated = translateWorkCategory(ctx, item.category)
                    val nameTranslated = translateWorkName(ctx, item.name)
                    val nameText = truncateName("$catTranslated: $nameTranslated", paintTable)
                    page.canvas.drawText("$rowNum", COL_NO_X, rowBaseline, paintTable)
                    page.canvas.drawText(nameText, COL_NAME_X, rowBaseline, paintTable)
                    page.canvas.drawText(item.unitAbbr, COL_UNIT_X, rowBaseline, paintTable)
                    paintTable.textAlign = Paint.Align.RIGHT
                    page.canvas.drawText("%.2f".format(item.quantity), COL_QTY_RIGHT, rowBaseline, paintTable)
                    page.canvas.drawText("%.2f".format(item.price), COL_PRICE_RIGHT, rowBaseline, paintTable)
                    page.canvas.drawText("%.2f".format(item.total), COL_SUM_RIGHT, rowBaseline, paintTable)
                    paintTable.textAlign = Paint.Align.LEFT
                    y += ROW_HEIGHT
                    page.canvas.drawLine(TABLE_LEFT, y, TABLE_RIGHT, y, paintGrid)
                    drawVerticalGrid(rowTop, y)
                }
                maybeNewPage()
                val sectionSum = section.items.sumOf { it.item.total }
                val sectionRowTop = y
                val sectionRowH = ROW_HEIGHT + 4
                page.canvas.drawRect(RectF(TABLE_LEFT, sectionRowTop, TABLE_RIGHT, sectionRowTop + sectionRowH), paintBlockBg)
                paintTable.getFontMetrics(fm)
                val sectionBaseline = sectionRowTop + sectionRowH - fm.descent
                paintTable.textAlign = Paint.Align.RIGHT
                page.canvas.drawText("${ctx.getString(ru.profia.app.R.string.estimate_total_per_room)}: ${"%.2f".format(sectionSum)} ₽", COL_SUM_RIGHT, sectionBaseline, paintTable)
                paintTable.textAlign = Paint.Align.LEFT
                y += sectionRowH
                page.canvas.drawLine(TABLE_LEFT, y, TABLE_RIGHT, y, paintGrid)
                drawVerticalGrid(sectionRowTop, y)
            }
            maybeNewPage()
            y += 4
            val totalRowTop = y
            val totalRowH = LINE_HEIGHT + 4f
            val computedTotal = sections.flatMap { it.items }.sumOf { it.item.total }
            page.canvas.drawRect(RectF(TABLE_LEFT, totalRowTop, TABLE_RIGHT, totalRowTop + totalRowH), paintBlockBg)
            page.canvas.drawText("${ctx.getString(ru.profia.app.R.string.estimate_total_works_label)}:", COL_NAME_X, totalRowTop + 14f, paintTitle)
            paintTitle.textAlign = Paint.Align.RIGHT
            page.canvas.drawText("${"%.2f".format(computedTotal)} ₽", COL_SUM_RIGHT, totalRowTop + 14f, paintTitle)
            paintTitle.textAlign = Paint.Align.LEFT
            page.canvas.drawLine(TABLE_LEFT, totalRowTop, TABLE_RIGHT, totalRowTop, paintGrid)
            page.canvas.drawLine(TABLE_LEFT, totalRowTop + totalRowH, TABLE_RIGHT, totalRowTop + totalRowH, paintGrid)
            drawVerticalGrid(totalRowTop, totalRowTop + totalRowH)
            y = totalRowTop + totalRowH + LINE_HEIGHT
            val signY = (PDF_PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 2).toFloat()
            page.canvas.drawText("${ctx.getString(ru.profia.app.R.string.estimate_customer)}: _________________________", MARGIN.toFloat(), signY, paintTable)
            page.canvas.drawText("${ctx.getString(ru.profia.app.R.string.estimate_contractor)}: _________________________", (PDF_PAGE_WIDTH / 2 + 20).toFloat(), signY, paintTable)
            doc.finishPage(page)
            FileOutputStream(file).use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun exportToCsv(
        context: Context,
        sections: List<EstimateRoomSection>,
        @Suppress("UNUSED_PARAMETER") totalSum: Double,
        title: String = "Предварительная смета",
        filePrefix: String = "smeta",
        fileName: String = "${filePrefix}_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv",
        customerLines: List<String>? = null,
        executorLines: List<String>? = null,
        exportLocale: Locale = Locale.getDefault(),
        titleResId: Int? = null
    ): File? {
        val config = Configuration(context.resources.configuration).apply { setLocale(exportLocale) }
        val ctx = context.createConfigurationContext(config)
        val displayTitle = if (titleResId != null) ctx.getString(titleResId) else title
        return try {
            val file = File(context.cacheDir, fileName)
            val sb = StringBuilder()
            sb.append('\uFEFF')
            sb.append(ExportUtils.escapeCsv(displayTitle)).append("\r\n")
            val maxLines = maxOf(customerLines?.size ?: 0, executorLines?.size ?: 0)
            if (maxLines > 0) {
                for (i in 0 until maxLines) {
                    sb.append(ExportUtils.escapeCsv(customerLines?.getOrNull(i) ?: ""))
                    sb.append(";")
                    sb.append(ExportUtils.escapeCsv(executorLines?.getOrNull(i) ?: ""))
                    sb.append("\r\n")
                }
                sb.append(";\r\n")
            }
            sb.append(ctx.getString(ru.profia.app.R.string.estimate_csv_header)).append("\r\n")
            for (section in sections) {
                for (ui in section.items) {
                    val item = ui.item
                    sb.append(ExportUtils.escapeCsv(section.roomName))
                    sb.append(";")
                    sb.append(ExportUtils.escapeCsv(translateWorkCategory(ctx, item.category)))
                    sb.append(";")
                    sb.append(ExportUtils.escapeCsv(translateWorkName(ctx, item.name)))
                    sb.append(";")
                    sb.append("%.2f".format(item.quantity))
                    sb.append(";")
                    sb.append(ExportUtils.escapeCsv(item.unitAbbr))
                    sb.append(";")
                    sb.append("%.2f".format(item.price))
                    sb.append(";")
                    sb.append("%.2f".format(item.total))
                    sb.append("\r\n")
                }
                val sectionSum = section.items.sumOf { it.item.total }
                sb.append(";").append(ctx.getString(ru.profia.app.R.string.estimate_total_per_room)).append(";;;;;")
                sb.append("%.2f".format(sectionSum))
                sb.append("\r\n")
            }
            val computedTotal = sections.flatMap { it.items }.sumOf { it.item.total }
            sb.append(";;;;;;")
            sb.append("%.2f".format(computedTotal))
            sb.append("\r\n")
            file.writeText(sb.toString(), Charsets.UTF_8)
            file
        } catch (e: Exception) {
            null
        }
    }

    /** Экспорт одного акта (промежуточной сметы) в PDF. Язык сметы задаётся exportLocale (не зависит от языка меню). */
    fun exportActToPdf(
        context: Context,
        actTitle: String,
        items: List<IntermediateEstimateActItemEntity>,
        @Suppress("UNUSED_PARAMETER") totalSum: Double,
        fileName: String = "akt_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf",
        customerLines: List<String>? = null,
        executorLines: List<String>? = null,
        discountText: String? = null,
        taxPercentText: String? = null,
        exportLocale: Locale = Locale.getDefault()
    ): File? {
        val filteredItems = items.filter { !isBaseProfitRow(it.name) }
        if (filteredItems.isEmpty()) return null
        val config = Configuration(context.resources.configuration).apply { setLocale(exportLocale) }
        val ctx = context.createConfigurationContext(config)
        return try {
            val file = File(context.cacheDir, fileName)
            val doc = PdfDocument()
            val sansSerifAct = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val sansSerifBoldAct = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            val paintTitle = Paint().apply { textSize = TITLE_SIZE; setTypeface(sansSerifBoldAct) }
            val paintSmall = Paint().apply { textSize = 9f; setTypeface(sansSerifAct); color = Color.DKGRAY }
            val paintGridAct = Paint().apply {
                color = GRID_COLOR
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
                isAntiAlias = true
            }
            val paintBlockBgAct = Paint().apply { color = BLOCK_BG_COLOR; style = Paint.Style.FILL }
            var page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
            var y = MARGIN.toFloat()

            fun drawText(canvas: android.graphics.Canvas, text: String, paint: Paint) {
                canvas.drawText(text, MARGIN.toFloat(), y, paint)
                y += LINE_HEIGHT
            }
            fun drawTextCentered(canvas: android.graphics.Canvas, text: String, paint: Paint) {
                val x = (PDF_PAGE_WIDTH / 2 - paint.measureText(text) / 2).toFloat()
                canvas.drawText(text, x, y, paint)
                y += LINE_HEIGHT
            }
            fun maybeNewPage() {
                if (y > (PDF_PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 3)) {
                    doc.finishPage(page)
                    page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
                    y = MARGIN.toFloat()
                }
            }
            val colXListAct = floatArrayOf(TABLE_LEFT, COL_NO_RIGHT, COL_NAME_X, COL_NAME_RIGHT, COL_UNIT_X, COL_UNIT_RIGHT, COL_QTY_RIGHT, COL_PRICE_RIGHT, COL_SUM_RIGHT, TABLE_RIGHT)
            fun drawVerticalGridAct(fromY: Float, toY: Float) {
                for (x in colXListAct) page.canvas.drawLine(x, fromY, x, toY, paintGridAct)
            }

            drawTextCentered(page.canvas, actTitle, paintTitle)
            y += LINE_HEIGHT
            val rightColumnX = PDF_PAGE_WIDTH - MARGIN
            val midXAct = PDF_PAGE_WIDTH / 2f
            if (!customerLines.isNullOrEmpty() || !executorLines.isNullOrEmpty()) {
                val blockTopAct = y
                val maxLines = maxOf(customerLines?.size ?: 0, executorLines?.size ?: 0)
                val lineStepAct = (LINE_HEIGHT * 0.9f).toInt().toFloat()
                val blockBottomAct = blockTopAct + maxLines * lineStepAct + 4f
                page.canvas.drawRect(RectF(MARGIN.toFloat(), blockTopAct - 2, midXAct - 8, blockBottomAct), paintBlockBgAct)
                page.canvas.drawRect(RectF(midXAct + 8, blockTopAct - 2, rightColumnX.toFloat(), blockBottomAct), paintBlockBgAct)
                for (i in 0 until maxLines) {
                    val leftText = customerLines?.getOrNull(i) ?: ""
                    val rightText = executorLines?.getOrNull(i) ?: ""
                    if (leftText.isNotEmpty() || rightText.isNotEmpty()) {
                        page.canvas.drawText(leftText, MARGIN.toFloat(), y, paintSmall)
                        if (rightText.isNotEmpty()) {
                            page.canvas.drawText(rightText, rightColumnX - paintSmall.measureText(rightText), y, paintSmall)
                        }
                        y += lineStepAct
                    }
                }
                y = blockBottomAct + LINE_HEIGHT
            }
            val paintTableAct = Paint().apply { textSize = 9f; setTypeface(sansSerifAct) }
            val paintTableBoldAct = Paint().apply { textSize = 9f; setTypeface(sansSerifBoldAct) }
            val paintRoomNameAct = Paint().apply { textSize = 9f; setTypeface(sansSerifAct); color = Color.GRAY }
            val fmAct = Paint.FontMetrics()
            fun baselineForRow(rowTop: Float, paint: Paint): Float {
                paint.getFontMetrics(fmAct)
                return rowTop + ROW_HEIGHT - fmAct.descent
            }
            val tableHeaderYAct = y
            val headerBaselineAct = baselineForRow(tableHeaderYAct, paintTableBoldAct)
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_no), COL_NO_X, headerBaselineAct, paintTableBoldAct)
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_work_name), COL_NAME_X, headerBaselineAct, paintTableBoldAct)
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_unit), COL_UNIT_X, headerBaselineAct, paintTableBoldAct)
            paintTableBoldAct.textAlign = Paint.Align.RIGHT
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_qty), COL_QTY_RIGHT, headerBaselineAct, paintTableBoldAct)
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_price), COL_PRICE_RIGHT, headerBaselineAct, paintTableBoldAct)
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_col_sum), COL_SUM_RIGHT, headerBaselineAct, paintTableBoldAct)
            paintTableBoldAct.textAlign = Paint.Align.LEFT
            y += ROW_HEIGHT + 4
            page.canvas.drawLine(TABLE_LEFT, tableHeaderYAct - 10f, TABLE_RIGHT, tableHeaderYAct - 10f, paintGridAct)
            page.canvas.drawLine(TABLE_LEFT, y, TABLE_RIGHT, y, paintGridAct)
            drawVerticalGridAct(tableHeaderYAct - 10f, y)
            var actRowNum = 0
            fun truncateNameAct(text: String, p: Paint): String {
                if (p.measureText(text) <= COL_NAME_W) return text
                var s = text
                while (s.length > 1 && p.measureText("$s…") > COL_NAME_W) s = s.dropLast(1)
                return if (s.length < text.length) "$s…" else text
            }
            val byRoom = filteredItems.groupBy { it.roomName }
            for ((roomName, roomItems) in byRoom) {
                maybeNewPage()
                val roomRowTopAct = y
                page.canvas.drawLine(TABLE_LEFT, roomRowTopAct, TABLE_RIGHT, roomRowTopAct, paintGridAct)
                y += ROW_HEIGHT
                val roomRowBottomAct = y
                paintRoomNameAct.getFontMetrics(fmAct)
                val roomBaseline = roomRowBottomAct - fmAct.descent
                val roomNameShort = truncateNameAct(roomName, paintRoomNameAct)
                page.canvas.drawText(roomNameShort, COL_NAME_X, roomBaseline, paintRoomNameAct)
                page.canvas.drawLine(TABLE_LEFT, roomRowBottomAct, TABLE_RIGHT, roomRowBottomAct, paintGridAct)
                drawVerticalGridAct(roomRowTopAct, roomRowBottomAct)
                for (item in roomItems) {
                    maybeNewPage()
                    actRowNum++
                    val rowTopAct = y
                    val rowBaselineAct = baselineForRow(rowTopAct, paintTableAct)
                    val catTranslated = translateWorkCategory(ctx, item.category)
                    val nameTranslated = translateWorkName(ctx, item.name)
                    val nm = catTranslated + ": " + nameTranslated
                    val nmShort = if (paintTableAct.measureText(nm) > COL_NAME_W) nm.take(35) + ".." else nm
                    page.canvas.drawText(actRowNum.toString(), COL_NO_X, rowBaselineAct, paintTableAct)
                    page.canvas.drawText(nmShort, COL_NAME_X, rowBaselineAct, paintTableAct)
                    page.canvas.drawText(item.unitAbbr, COL_UNIT_X, rowBaselineAct, paintTableAct)
                    paintTableAct.textAlign = Paint.Align.RIGHT
                    page.canvas.drawText("%.2f".format(item.quantity), COL_QTY_RIGHT, rowBaselineAct, paintTableAct)
                    page.canvas.drawText("%.2f".format(item.price), COL_PRICE_RIGHT, rowBaselineAct, paintTableAct)
                    page.canvas.drawText("%.2f".format(item.total), COL_SUM_RIGHT, rowBaselineAct, paintTableAct)
                    paintTableAct.textAlign = Paint.Align.LEFT
                    y += ROW_HEIGHT
                    page.canvas.drawLine(TABLE_LEFT, y, TABLE_RIGHT, y, paintGridAct)
                    drawVerticalGridAct(rowTopAct, y)
                }
                maybeNewPage()
                val sectionSum = roomItems.sumOf { it.total }
                val sectionRowTopAct = y
                val sectionRowHAct = ROW_HEIGHT + 4
                page.canvas.drawRect(RectF(TABLE_LEFT, sectionRowTopAct, TABLE_RIGHT, sectionRowTopAct + sectionRowHAct), paintBlockBgAct)
                paintTableAct.getFontMetrics(fmAct)
                val sectionBaselineAct = sectionRowTopAct + sectionRowHAct - fmAct.descent
                paintTableAct.textAlign = Paint.Align.RIGHT
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_total_per_room) + ": " + "%.2f".format(sectionSum) + " ₽", COL_SUM_RIGHT, sectionBaselineAct, paintTableAct)
                paintTableAct.textAlign = Paint.Align.LEFT
                y += sectionRowHAct
                page.canvas.drawLine(TABLE_LEFT, y, TABLE_RIGHT, y, paintGridAct)
                // Без вертикальных перегородок в строке «Итого по помещению» (Total cameră)
            }
            maybeNewPage()
            y += 4
            val discountPercent = parsePercentFromDiscountText(discountText)
            val taxPercent = parsePercentFromTaxText(taxPercentText)
            val totalRowTopAct = y
            val totalRowHAct = LINE_HEIGHT + 4f
            val computedTotalAct = filteredItems.sumOf { it.total }
            var runningTotal = computedTotalAct
            page.canvas.drawRect(RectF(TABLE_LEFT, totalRowTopAct, TABLE_RIGHT, totalRowTopAct + totalRowHAct), paintBlockBgAct)
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_total_works_label) + ":", COL_NAME_X, totalRowTopAct + 14f, paintTitle)
            paintTitle.textAlign = Paint.Align.RIGHT
            page.canvas.drawText("%.2f".format(computedTotalAct) + " ₽", COL_SUM_RIGHT, totalRowTopAct + 14f, paintTitle)
            paintTitle.textAlign = Paint.Align.LEFT
            page.canvas.drawLine(TABLE_LEFT, totalRowTopAct, TABLE_RIGHT, totalRowTopAct, paintGridAct)
            page.canvas.drawLine(TABLE_LEFT, totalRowTopAct + totalRowHAct, TABLE_RIGHT, totalRowTopAct + totalRowHAct, paintGridAct)
            // Без вертикальных перегородок в строке «Итого по работам» (Total lucrări:)
            y = totalRowTopAct + totalRowHAct + LINE_HEIGHT
            if (discountPercent != null && discountPercent > 0) {
                val discountAmount = runningTotal * (discountPercent / 100.0)
                runningTotal -= discountAmount
                val rowDiscountTop = y
                val rowDiscountH = LINE_HEIGHT + 4f
                page.canvas.drawRect(RectF(TABLE_LEFT, rowDiscountTop, TABLE_RIGHT, rowDiscountTop + rowDiscountH), paintBlockBgAct)
                val labelDiscount = ctx.getString(ru.profia.app.R.string.estimate_discount_amount_label)
                page.canvas.drawText(labelDiscount, COL_NAME_X, rowDiscountTop + 14f, paintTitle)
                paintTitle.textAlign = Paint.Align.RIGHT
                page.canvas.drawText("%.2f".format(discountAmount) + " ₽", COL_SUM_RIGHT, rowDiscountTop + 14f, paintTitle)
                paintTitle.textAlign = Paint.Align.LEFT
                page.canvas.drawLine(TABLE_LEFT, rowDiscountTop, TABLE_RIGHT, rowDiscountTop, paintGridAct)
                page.canvas.drawLine(TABLE_LEFT, rowDiscountTop + rowDiscountH, TABLE_RIGHT, rowDiscountTop + rowDiscountH, paintGridAct)
                // Без вертикальных перегородок в строке скидки (IE amount)
                y = rowDiscountTop + rowDiscountH + LINE_HEIGHT
            }
            if (taxPercent != null && taxPercent > 0) {
                val taxAmount = runningTotal * (taxPercent / 100.0)
                runningTotal += taxAmount
                val rowTaxTop = y
                val rowTaxH = LINE_HEIGHT + 4f
                page.canvas.drawRect(RectF(TABLE_LEFT, rowTaxTop, TABLE_RIGHT, rowTaxTop + rowTaxH), paintBlockBgAct)
                val labelTaxAmount = ctx.getString(ru.profia.app.R.string.estimate_tax_amount_label, taxPercent.toInt().toString())
                page.canvas.drawText(labelTaxAmount, COL_NAME_X, rowTaxTop + 14f, paintTitle)
                paintTitle.textAlign = Paint.Align.RIGHT
                page.canvas.drawText("%.2f".format(taxAmount) + " ₽", COL_SUM_RIGHT, rowTaxTop + 14f, paintTitle)
                paintTitle.textAlign = Paint.Align.LEFT
                page.canvas.drawLine(TABLE_LEFT, rowTaxTop, TABLE_RIGHT, rowTaxTop, paintGridAct)
                page.canvas.drawLine(TABLE_LEFT, rowTaxTop + rowTaxH, TABLE_RIGHT, rowTaxTop + rowTaxH, paintGridAct)
                // Без вертикальных перегородок в строке НДС (Tax amount)
                y = rowTaxTop + rowTaxH + LINE_HEIGHT
            }
            if (discountPercent != null && discountPercent > 0 || (taxPercent != null && taxPercent > 0)) {
                val rowPayTop = y
                val rowPayH = LINE_HEIGHT + 4f
                page.canvas.drawRect(RectF(TABLE_LEFT, rowPayTop, TABLE_RIGHT, rowPayTop + rowPayH), paintBlockBgAct)
                page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_total_to_pay), COL_NAME_X, rowPayTop + 14f, paintTitle)
                paintTitle.textAlign = Paint.Align.RIGHT
                page.canvas.drawText("%.2f".format(runningTotal) + " ₽", COL_SUM_RIGHT, rowPayTop + 14f, paintTitle)
                paintTitle.textAlign = Paint.Align.LEFT
                y = rowPayTop + rowPayH + LINE_HEIGHT
            }
            val signYAct = (PDF_PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 2).toFloat()
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_customer) + ": _________________________", MARGIN.toFloat(), signYAct, paintTableAct)
            page.canvas.drawText(ctx.getString(ru.profia.app.R.string.estimate_contractor) + ": _________________________", (PDF_PAGE_WIDTH / 2 + 20).toFloat(), signYAct, paintTableAct)
            doc.finishPage(page)
            FileOutputStream(file).use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    /** Экспорт одного акта в CSV (Excel). Язык сметы задаётся exportLocale (не зависит от языка меню). */
    fun exportActToCsv(
        context: Context,
        actTitle: String,
        items: List<IntermediateEstimateActItemEntity>,
        @Suppress("UNUSED_PARAMETER") totalSum: Double,
        fileName: String = "akt_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv",
        customerLines: List<String>? = null,
        executorLines: List<String>? = null,
        discountText: String? = null,
        taxPercentText: String? = null,
        exportLocale: Locale = Locale.getDefault()
    ): File? {
        val filteredItems = items.filter { !isBaseProfitRow(it.name) }
        if (filteredItems.isEmpty()) return null
        val config = Configuration(context.resources.configuration).apply { setLocale(exportLocale) }
        val ctx = context.createConfigurationContext(config)
        return try {
            val file = File(context.cacheDir, fileName)
            val sb = StringBuilder()
            sb.append('\uFEFF')
            sb.append(ExportUtils.escapeCsv(actTitle)).append("\r\n")
            val maxLines = maxOf(customerLines?.size ?: 0, executorLines?.size ?: 0)
            if (maxLines > 0) {
                for (i in 0 until maxLines) {
                    sb.append(ExportUtils.escapeCsv(customerLines?.getOrNull(i) ?: "")).append(";")
                    sb.append(ExportUtils.escapeCsv(executorLines?.getOrNull(i) ?: "")).append("\r\n")
                }
                sb.append("\r\n")
            }
            sb.append(ctx.getString(ru.profia.app.R.string.estimate_csv_header)).append("\r\n")
            val byRoom = filteredItems.groupBy { it.roomName }
            for ((roomName, roomItems) in byRoom) {
                for (item in roomItems) {
                    sb.append(ExportUtils.escapeCsv(roomName)); sb.append(";")
                    sb.append(ExportUtils.escapeCsv(translateWorkCategory(ctx, item.category))); sb.append(";")
                    sb.append(ExportUtils.escapeCsv(translateWorkName(ctx, item.name))); sb.append(";")
                    sb.append("%.2f".format(item.quantity)); sb.append(";")
                    sb.append(ExportUtils.escapeCsv(item.unitAbbr)); sb.append(";")
                    sb.append("%.2f".format(item.price)); sb.append(";")
                    sb.append("%.2f".format(item.total)); sb.append("\r\n")
                }
                val sectionSum = roomItems.sumOf { it.total }
                sb.append(";").append(ctx.getString(ru.profia.app.R.string.estimate_total_per_room)).append(";;;;;").append("%.2f".format(sectionSum)).append("\r\n")
            }
            val computedTotalAct = filteredItems.sumOf { it.total }
            val discountPercent = parsePercentFromDiscountText(discountText)
            val taxPercent = parsePercentFromTaxText(taxPercentText)
            sb.append(ExportUtils.escapeCsv(ctx.getString(ru.profia.app.R.string.estimate_total_works_label))).append(";;;;;;").append("%.2f".format(computedTotalAct)).append("\r\n")
            var runningTotal = computedTotalAct
            if (discountPercent != null && discountPercent > 0) {
                val discountAmount = runningTotal * (discountPercent / 100.0)
                runningTotal -= discountAmount
                val labelDiscount = ctx.getString(ru.profia.app.R.string.estimate_discount_amount_label)
                sb.append(ExportUtils.escapeCsv(labelDiscount)).append(";;;;;;").append("%.2f".format(discountAmount)).append("\r\n")
            }
            if (taxPercent != null && taxPercent > 0) {
                val taxAmount = runningTotal * (taxPercent / 100.0)
                runningTotal += taxAmount
                val labelTaxAmount = ctx.getString(ru.profia.app.R.string.estimate_tax_amount_label, taxPercent.toInt().toString())
                sb.append(ExportUtils.escapeCsv(labelTaxAmount)).append(";;;;;;").append("%.2f".format(taxAmount)).append("\r\n")
            }
            if (discountPercent != null && discountPercent > 0 || (taxPercent != null && taxPercent > 0)) {
                sb.append(ExportUtils.escapeCsv(ctx.getString(ru.profia.app.R.string.estimate_total_to_pay))).append(";;;;;;").append("%.2f".format(runningTotal)).append("\r\n")
            }
            file.writeText(sb.toString(), Charsets.UTF_8)
            file
        } catch (e: Exception) {
            null
        }
    }

}
