package ru.profia.app.ui.export

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** A4 portrait (для КС-3 и общего fallback). */
private const val PDF_PAGE_WIDTH_PORTRAIT = 595
private const val PDF_PAGE_HEIGHT_PORTRAIT = 842
/** A4 альбом (горизонтально) — по форме КС-2 (ГОСТ). */
private const val PDF_PAGE_WIDTH = 842
private const val PDF_PAGE_HEIGHT = 595
private const val MARGIN = 36
private const val MARGIN_F = 36f
private const val LINE_HEIGHT = 12
private const val LINE_HEIGHT_SMALL = 10
private const val LINE_HEIGHT_F = 12f
private const val LINE_HEIGHT_SMALL_F = 10f
private const val TITLE_SIZE = 12f
private const val BODY_SIZE = 8f
private const val SMALL_SIZE = 7f

/**
 * Экспорт форм КС-2 (акт приёмки выполненных работ) и КС-3 (справка о стоимости).
 * PROFI — в акте только профиль (ФИО, тел., email); BUSINESS — профиль + компания + реквизиты.
 */
object Ks2Ks3Export {

    fun exportKs2Pdf(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        accountType: String = "PROFI",
        fileName: String = "KS-2_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf"
    ): File? = exportFormPdf(context, profile, act, items, totalSum, "КС-2. Акт приёмки выполненных работ", fileName, accountType)

    fun exportKs3Pdf(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        accountType: String = "PROFI",
        fileName: String = "KS-3_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf"
    ): File? = exportFormPdf(context, profile, act, items, totalSum, "КС-3. Справка о стоимости выполненных работ и затрат", fileName, accountType)

    private fun executorLines(profile: UserProfile, accountType: String): List<String> {
        val isProfi = accountType == "PROFI"
        val name = if (isProfi) {
            listOf(profile.lastName, profile.firstName, profile.middleName.orEmpty()).filter { it.isNotBlank() }.joinToString(" ")
        } else {
            profile.companyName?.takeIf { it.isNotBlank() }
                ?: listOf(profile.lastName, profile.firstName, profile.middleName.orEmpty()).filter { it.isNotBlank() }.joinToString(" ")
        }
        val lines = mutableListOf<String>()
        if (name.isNotBlank()) lines.add(name)
        if (!isProfi) {
            profile.inn?.takeIf { it.isNotBlank() }?.let { lines.add("ИНН $it") }
            profile.kpp?.takeIf { it.isNotBlank() }?.let { lines.add("КПП $it") }
            profile.legalAddress?.takeIf { it.isNotBlank() }?.let { lines.add("Адрес: $it") }
            profile.bankName?.takeIf { it.isNotBlank() }?.let { lines.add("Банк: $it") }
            profile.accountNumber?.takeIf { it.isNotBlank() }?.let { lines.add("Р/с: $it") }
            profile.correspondentAccount?.takeIf { it.isNotBlank() }?.let { lines.add("К/с: $it") }
            profile.bic?.takeIf { it.isNotBlank() }?.let { lines.add("БИК: $it") }
        }
        profile.phone.takeIf { it.isNotBlank() }?.let { lines.add("Тел.: $it") }
        profile.email.takeIf { it.isNotBlank() }?.let { lines.add("Email: $it") }
        return lines
    }

    private fun exportFormPdf(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        formTitle: String,
        fileName: String,
        accountType: String = "PROFI"
    ): File? {
        if (items.isEmpty()) return null
        return when {
            formTitle.contains("КС-2") -> exportKs2PdfUnifiedForm(context, profile, act, items, totalSum, fileName, accountType)
            formTitle.contains("КС-3") -> exportKs3PdfUnifiedForm(context, profile, act, items, totalSum, fileName, accountType)
            else -> exportFormPdfSimple(context, profile, act, items, totalSum, formTitle, fileName, accountType)
        }
    }

    /** Экспорт КС-2 по унифицированной форме № КС-2: А4 альбом, структура по постановлению Госкомстата 11.11.99 № 100. */
    private fun exportKs2PdfUnifiedForm(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        fileName: String,
        accountType: String
    ): File? {
        return try {
            val file = File(context.cacheDir, fileName)
            val doc = PdfDocument()
            val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val cal = Calendar.getInstance().apply { timeInMillis = act.createdAt }
            val reportStart = cal.apply { set(Calendar.DAY_OF_MONTH, 1) }.time
            val reportEnd = cal.apply { set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)) }.time

            val paintSmall = Paint().apply { textSize = SMALL_SIZE; isAntiAlias = true }
            val paintBody = Paint().apply { textSize = BODY_SIZE; isAntiAlias = true }
            val paintBold = Paint().apply {
                textSize = BODY_SIZE
                isFakeBoldText = true
                setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
                isAntiAlias = true
            }
            val paintTitle = Paint().apply {
                textSize = TITLE_SIZE
                isFakeBoldText = true
                setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
                isAntiAlias = true
            }

            val page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
            val canvas = page.canvas
            var y = MARGIN_F

            fun drawLeft(text: String, paint: Paint, indent: Float = 0f) {
                canvas.drawText(text, MARGIN_F + indent, y, paint)
                y += LINE_HEIGHT_SMALL_F
            }
            fun drawRight(text: String, paint: Paint) {
                val x = (PDF_PAGE_WIDTH - MARGIN).toFloat() - paint.measureText(text)
                canvas.drawText(text, x, y, paint)
                y += LINE_HEIGHT_SMALL_F
            }
            fun drawCenter(text: String, paint: Paint) {
                val x = (PDF_PAGE_WIDTH - paint.measureText(text)) / 2f
                canvas.drawText(text, x, y, paint)
                y += LINE_HEIGHT_F
            }
            fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
                canvas.drawLine(x1, y1, x2, y2, paintBody)
            }

            // Верх: справа — форма и коды
            val rightX = (PDF_PAGE_WIDTH - MARGIN).toFloat() - 120f
            y = MARGIN_F + LINE_HEIGHT_SMALL_F
            canvas.drawText("Унифицированная форма № КС-2", rightX, y, paintSmall)
            y += LINE_HEIGHT_SMALL_F
            canvas.drawText("Утверждена постановлением Госкомстата России от 11.11.99 № 100", rightX - 20f, y, paintSmall)
            y += LINE_HEIGHT_SMALL_F * 2f
            canvas.drawText("Код", rightX + 40f, y, paintSmall)
            y += LINE_HEIGHT_SMALL_F
            canvas.drawText("Форма по ОКУД  0322005", rightX, y, paintSmall)
            y += LINE_HEIGHT_SMALL_F
            canvas.drawText("по ОКПО  ____________  ____________  ____________", rightX - 10f, y, paintSmall)
            val headerBottom = y + 8f
            y = MARGIN_F

            // Левая колонка: Инвестор, Заказчик, Подрядчик, Стройка, Объект
            drawLeft("Инвестор (организация, адрес, телефон, факс): _________________________", paintSmall)
            drawLeft("Заказчик (Генподрядчик) (организация, адрес, телефон, факс): ___________", paintSmall)
            drawLeft(context.getString(ru.profia.app.R.string.estimate_contractor) + " (Субподрядчик):", paintSmall)
            executorLines(profile, accountType).forEach { drawLeft(it, paintSmall, 8f) }
            drawLeft("Стройка (наименование, адрес): _________________________________________", paintSmall)
            drawLeft("Объект (наименование): ${act.title.take(50)}", paintSmall)
            y += 4f

            // Справа от середины: вид деятельности, договор, период
            val midX = PDF_PAGE_WIDTH / 2f - 20f
            y = headerBottom - LINE_HEIGHT_SMALL_F * 6f
            var yRight = MARGIN_F + LINE_HEIGHT_SMALL_F
            canvas.drawText("Вид деятельности по ОКДП ________________", midX, yRight, paintSmall)
            yRight += LINE_HEIGHT_SMALL_F
            canvas.drawText("Договор подряда (контракт)  № ________  от ________", midX, yRight, paintSmall)
            yRight += LINE_HEIGHT_SMALL_F
            canvas.drawText("Вид операции ________________", midX, yRight, paintSmall)
            yRight += LINE_HEIGHT_SMALL_F
            canvas.drawText("Отчетный период  с ${dateFmt.format(reportStart)}  по ${dateFmt.format(reportEnd)}", midX, yRight, paintSmall)

            // Центр: АКТ О ПРИЕМКЕ ВЫПОЛНЕННЫХ РАБООТ
            y = headerBottom + 6f
            drawCenter("АКТ", paintTitle)
            drawCenter("О ПРИЕМКЕ ВЫПОЛНЕННЫХ РАБОТ", paintTitle)
            y += 4f
            val costText = "Сметная (договорная) стоимость в соответствии с договором подряда (субподряда) ________________ ${"%.2f".format(totalSum)} руб."
            canvas.drawText(costText.take(90), MARGIN_F, y, paintBody)
            y += LINE_HEIGHT_F + 4f

            // Таблица: 8 колонок по форме КС-2
            val col1 = MARGIN_F
            val col2 = col1 + 32f
            val col3 = col2 + 38f
            val col4 = col3 + 220f
            val col5 = col4 + 42f
            val col6 = col5 + 38f
            val col7 = col6 + 48f
            val col8 = col7 + 52f
            val tableRight = (PDF_PAGE_WIDTH - MARGIN).toFloat()
            val rowH = LINE_HEIGHT_F + 2f

            fun tableRow(v1: String, v2: String, v3: String, v4: String, v5: String, v6: String, v7: String, v8: String, paint: Paint) {
                val nameMaxLen = 42
                val v3Short = if (v3.length > nameMaxLen) v3.take(nameMaxLen - 1) + "…" else v3
                canvas.drawText(v1, col1, y, paint)
                canvas.drawText(v2, col2, y, paint)
                canvas.drawText(v3Short, col3, y, paint)
                canvas.drawText(v4, col4, y, paint)
                canvas.drawText(v5, col5, y, paint)
                canvas.drawText(v6, col6, y, paint)
                canvas.drawText(v7, col7, y, paint)
                canvas.drawText(v8, tableRight - paint.measureText(v8), y, paint)
                y += rowH
            }

            drawLine(col1, y, tableRight, y)
            y += 2f
            tableRow("1", "2", "3", "4", "5", "6", "7", "8", paintSmall)
            tableRow("Номер", "Номер позиции", "Наименование работ", "Номер единичной", "Ед.", "количество", "цена за ед.,", "стоимость,", paintSmall)
            tableRow("по порядку", "по смете", "", "расценки", "изм.", "", "руб.", "руб.", paintSmall)
            y += 2f
            drawLine(col1, y, tableRight, y)
            y += rowH

            items.forEachIndexed { index, item ->
                val name = "${item.category}: ${item.name}"
                tableRow(
                    "${index + 1}",
                    "${index + 1}",
                    name,
                    "",
                    item.unitAbbr,
                    "%.2f".format(item.quantity),
                    "%.2f".format(item.price),
                    "%.2f".format(item.total),
                    paintBody
                )
            }

            drawLine(col1, y, tableRight, y)
            tableRow("", "", "Итого", "", "", "", "X", "%.2f".format(totalSum), paintBold)
            drawLine(col1, y, tableRight, y)

            doc.finishPage(page)
            FileOutputStream(file).use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    /** Экспорт КС-3 по унифицированной форме № КС-3: А4 альбом, постановление Госкомстата 11.11.99 № 100. */
    private fun exportKs3PdfUnifiedForm(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        fileName: String,
        accountType: String
    ): File? {
        return try {
            val file = File(context.cacheDir, fileName)
            val doc = PdfDocument()
            val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val cal = Calendar.getInstance().apply { timeInMillis = act.createdAt }
            val reportStart = cal.apply { set(Calendar.DAY_OF_MONTH, 1) }.time
            val reportEnd = cal.apply { set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)) }.time

            val paintSmall = Paint().apply { textSize = SMALL_SIZE; isAntiAlias = true }
            val paintBody = Paint().apply { textSize = BODY_SIZE; isAntiAlias = true }
            val paintBold = Paint().apply {
                textSize = BODY_SIZE
                isFakeBoldText = true
                setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
                isAntiAlias = true
            }
            val paintTitle = Paint().apply {
                textSize = TITLE_SIZE
                isFakeBoldText = true
                setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
                isAntiAlias = true
            }

            val page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
            val canvas = page.canvas
            var y = MARGIN_F

            fun drawLeft(text: String, paint: Paint, indent: Float = 0f) {
                canvas.drawText(text, MARGIN_F + indent, y, paint)
                y += LINE_HEIGHT_SMALL_F
            }
            fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
                canvas.drawLine(x1, y1, x2, y2, paintBody)
            }

            // Верх справа: форма КС-3 и коды
            val rightX = (PDF_PAGE_WIDTH - MARGIN).toFloat() - 140f
            y = MARGIN_F + LINE_HEIGHT_SMALL_F
            canvas.drawText("Унифицированная форма № КС-3", rightX, y, paintSmall)
            y += LINE_HEIGHT_SMALL_F
            canvas.drawText("Утверждена Постановлением Госкомстата России от 11 ноября 1999 г. № 100", rightX - 30f, y, paintSmall)
            y += LINE_HEIGHT_SMALL_F * 2f
            canvas.drawText("Форма по ОКУД _________________", rightX, y, paintSmall)
            y += LINE_HEIGHT_SMALL_F
            canvas.drawText("Код  0322001", rightX + 50f, y, paintSmall)
            val headerBottom = y + 6f
            y = MARGIN_F

            // Левая колонка: Инвестор, Заказчик, Подрядчик, Стройка (по ОКПО у каждого)
            drawLeft("Инвестор (организация, адрес, телефон, факс): _________________________", paintSmall)
            drawLeft("по ОКПО ____________", paintSmall, 4f)
            drawLeft("Заказчик (Генподрядчик) (организация, адрес, телефон, факс): ___________", paintSmall)
            drawLeft("по ОКПО ____________", paintSmall, 4f)
            drawLeft(context.getString(ru.profia.app.R.string.estimate_contractor) + " (Субподрядчик):", paintSmall)
            executorLines(profile, accountType).forEach { drawLeft(it, paintSmall, 8f) }
            drawLeft("по ОКПО ____________", paintSmall, 4f)
            drawLeft("Стройка (наименование, адрес): ${act.title.take(45)}", paintSmall)
            y += 4f

            // Справа: вид деятельности, договор, номер/дата документа, отчётный период
            val midX = PDF_PAGE_WIDTH / 2f - 30f
            var yR = MARGIN_F + LINE_HEIGHT_SMALL_F
            canvas.drawText("Вид деятельности по ОКДП ________________", midX, yR, paintSmall)
            yR += LINE_HEIGHT_SMALL_F
            canvas.drawText("Договор подряда (контракт)  № ________  дата ________", midX, yR, paintSmall)
            yR += LINE_HEIGHT_SMALL_F
            canvas.drawText("Вид операции ________________", midX, yR, paintSmall)
            yR += LINE_HEIGHT_SMALL_F
            canvas.drawText("Номер документа ________", midX, yR, paintSmall)
            yR += LINE_HEIGHT_SMALL_F
            canvas.drawText("Дата составления ${dateFmt.format(Date(act.createdAt))}", midX, yR, paintSmall)
            yR += LINE_HEIGHT_SMALL_F
            canvas.drawText("Отчетный период  с ${dateFmt.format(reportStart)}  по ${dateFmt.format(reportEnd)}", midX, yR, paintSmall)

            // Заголовок таблицы
            y = headerBottom + 4f
            val centerTitle = "СПРАВКА О СТОИМОСТИ ВЫПОЛНЕННЫХ РАБОТ И ЗАТРАТ"
            val titleX = (PDF_PAGE_WIDTH - paintTitle.measureText(centerTitle)) / 2f
            canvas.drawText(centerTitle, titleX, y, paintTitle)
            y += LINE_HEIGHT_F + 6f

            // Таблица КС-3: 1-Номер по порядку, 2-Наименование, 3-Код, 4-стоимость (3 подколонки)
            val c1 = MARGIN_F
            val c2 = c1 + 28f
            val c3 = c2 + 240f
            val c4a = c3 + 32f
            val c4b = c4a + 95f
            val c4c = c4b + 95f
            val tableRight = (PDF_PAGE_WIDTH - MARGIN).toFloat()
            val rowH = LINE_HEIGHT_F + 2f

            drawLine(c1, y, tableRight, y)
            y += 2f
            canvas.drawText("1", c1 + 4f, y, paintSmall)
            canvas.drawText("2", c2 + 4f, y, paintSmall)
            canvas.drawText("Номер по порядку", c1, y + rowH, paintSmall)
            val nameHeader = "Наименование пусковых комплексов, этапов, объектов, видов выполненных работ, оборудования, затрат"
            canvas.drawText(nameHeader.take(55) + "…", c2, y + rowH, paintSmall)
            canvas.drawText("3", c3 + 4f, y, paintSmall)
            canvas.drawText("Код", c3, y + rowH, paintSmall)
            canvas.drawText("Стоимость выполненных работ и затрат, руб.", c4a, y + rowH, paintSmall)
            y += rowH * 2f
            canvas.drawText("с начала проведения работ", c4a, y, paintSmall)
            canvas.drawText("с начала года", c4b, y, paintSmall)
            canvas.drawText("в том числе за отчетный период", c4c, y, paintSmall)
            y += rowH + 2f
            drawLine(c1, y, tableRight, y)
            y += rowH

            val nameMaxLen = 38
            // Строка 1 — объект/первая запись
            canvas.drawText("1", c1 + 4f, y, paintBody)
            canvas.drawText(act.title.take(nameMaxLen), c2, y, paintBody)
            canvas.drawText("", c3, y, paintBody)
            canvas.drawText("", c4a, y, paintBody)
            canvas.drawText("", c4b, y, paintBody)
            canvas.drawText("", c4c, y, paintBody)
            y += rowH
            // Строка 2 — Всего работ и затрат
            canvas.drawText("2", c1 + 4f, y, paintBody)
            canvas.drawText("Всего работ и затрат, включаемых в стоимость работ", c2, y, paintBold)
            canvas.drawText("", c3, y, paintBody)
            canvas.drawText("", c4a, y, paintBody)
            canvas.drawText("", c4b, y, paintBody)
            canvas.drawText("%.2f".format(totalSum), c4c, y, paintBold)
            y += rowH
            canvas.drawText("в том числе:", c2, y, paintSmall)
            y += rowH
            // Детализация — виды выполненных работ
            items.forEachIndexed { index, item ->
                val name = "${item.category}: ${item.name}"
                val nameShort = if (name.length > nameMaxLen) name.take(nameMaxLen - 1) + "…" else name
                canvas.drawText("", c1, y, paintBody)
                canvas.drawText(nameShort, c2, y, paintBody)
                canvas.drawText("", c3, y, paintBody)
                canvas.drawText("", c4a, y, paintBody)
                canvas.drawText("", c4b, y, paintBody)
                canvas.drawText("%.2f".format(item.total), c4c, y, paintBody)
                y += rowH
            }
            if (items.isEmpty()) { canvas.drawText("и т.д.", c2, y, paintBody); y += rowH }

            y += 4f

            // Итого, НДС, Всего с учетом НДС
            drawLine(c1, y, tableRight, y)
            y += rowH
            canvas.drawText("Итого", c2 + 180f, y, paintBold)
            canvas.drawText("%.2f".format(totalSum), c4c, y, paintBold)
            y += rowH
            canvas.drawText("Сумма НДС", c2 + 180f, y, paintBody)
            canvas.drawText("0,00", c4c, y, paintBody)
            y += rowH
            canvas.drawText("Всего с учетом НДС", c2 + 160f, y, paintBold)
            canvas.drawText("%.2f".format(totalSum), c4c, y, paintBold)
            y += 12f

            // Подписи: Заказчик (Генподрядчик) и Подрядчик (Субподрядчик)
            val sigW = (PDF_PAGE_WIDTH - 2 * MARGIN - 24f) / 2f
            val sigLeft = MARGIN_F
            val sigRight = MARGIN_F + sigW + 24f
            canvas.drawText("Заказчик (Генподрядчик)", sigLeft, y, paintSmall)
            canvas.drawText(context.getString(ru.profia.app.R.string.estimate_contractor) + " (Субподрядчик)", sigRight, y, paintSmall)
            y += rowH + 4f
            canvas.drawText("должность ________________  подпись ________  расшифровка ________  М.П.", sigLeft, y, paintSmall)
            canvas.drawText("должность ________________  подпись ________  расшифровка ________  М.П.", sigRight, y, paintSmall)

            doc.finishPage(page)
            FileOutputStream(file).use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    /** Упрощённый экспорт (fallback): А4 альбом. */
    private fun exportFormPdfSimple(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        formTitle: String,
        fileName: String,
        accountType: String
    ): File? {
        return try {
            val file = File(context.cacheDir, fileName)
            val doc = PdfDocument()
            val paintTitle = Paint().apply {
                textSize = TITLE_SIZE
                isFakeBoldText = true
                setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
            }
            val paintBody = Paint().apply { textSize = BODY_SIZE }
            var page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
            var y = MARGIN_F

            fun draw(canvas: android.graphics.Canvas, text: String, paint: Paint) {
                canvas.drawText(text, MARGIN_F, y, paint)
                y += LINE_HEIGHT_F
            }
            fun maybeNewPage() {
                if (y > (PDF_PAGE_HEIGHT - MARGIN).toFloat() - LINE_HEIGHT_F * 4) {
                    doc.finishPage(page)
                    page = doc.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create())
                    y = MARGIN_F
                }
            }

            val canvas = page.canvas
            draw(canvas, formTitle, paintTitle)
            draw(canvas, "№ ___ от ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(act.createdAt))}", paintBody)
            y += LINE_HEIGHT_F
            draw(canvas, context.getString(ru.profia.app.R.string.estimate_contractor) + ":", paintBody)
            executorLines(profile, accountType).forEach { draw(canvas, it, paintBody) }
            draw(canvas, "Заказчик: _________________________________", paintBody)
            y += LINE_HEIGHT_F
            draw(canvas, "Наименование объекта: ${act.title}", paintBody)
            y += LINE_HEIGHT_F
            draw(canvas, "№ п/п | Наименование работ | Ед.изм. | Кол-во | Цена | Сумма", paintTitle)
            items.forEachIndexed { index, item ->
                maybeNewPage()
                val line = "${index + 1} | ${item.category}: ${item.name} | ${item.unitAbbr} | ${"%.2f".format(item.quantity)} | ${"%.2f".format(item.price)} | ${"%.2f".format(item.total)}"
                draw(canvas, line.take(100) + if (line.length > 100) "..." else "", paintBody)
            }
            maybeNewPage()
            y += LINE_HEIGHT_F
            draw(canvas, "Итого: ${"%.2f".format(totalSum)} ₽", paintTitle)
            doc.finishPage(page)
            FileOutputStream(file).use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun exportKs2Csv(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        accountType: String = "PROFI",
        fileName: String = "KS-2_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
    ): File? = exportFormCsv(context, profile, act, items, totalSum, "КС-2", fileName, accountType)

    fun exportKs3Csv(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        accountType: String = "PROFI",
        fileName: String = "KS-3_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
    ): File? = exportFormCsv(context, profile, act, items, totalSum, "КС-3", fileName, accountType)

    private fun exportFormCsv(
        context: Context,
        profile: UserProfile,
        act: IntermediateEstimateActEntity,
        items: List<IntermediateEstimateActItemEntity>,
        totalSum: Double,
        formName: String,
        fileName: String,
        accountType: String = "PROFI"
    ): File? {
        if (items.isEmpty()) return null
        return try {
            val file = File(context.cacheDir, fileName)
            val sb = StringBuilder()
            sb.append('\uFEFF')
            sb.append("$formName;Акт/Справка\r\n")
            sb.append("Дата;${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(act.createdAt))}\r\n")
            val execLines = executorLines(profile, accountType)
            val nameLine = execLines.firstOrNull() ?: ""
            sb.append("${context.getString(ru.profia.app.R.string.estimate_contractor)};${escape(nameLine)}\r\n")
            if (accountType != "PROFI") {
                sb.append("ИНН;${profile.inn ?: ""}\r\n")
                sb.append("КПП;${profile.kpp ?: ""}\r\n")
                sb.append("Адрес;${escape(profile.legalAddress ?: "")}\r\n")
                profile.bankName?.takeIf { it.isNotBlank() }?.let { sb.append("Банк;${escape(it)}\r\n") }
                profile.accountNumber?.takeIf { it.isNotBlank() }?.let { sb.append("Р/с;${escape(it)}\r\n") }
                profile.bic?.takeIf { it.isNotBlank() }?.let { sb.append("БИК;${it}\r\n") }
            }
            profile.phone.takeIf { it.isNotBlank() }?.let { sb.append("Тел.;${escape(it)}\r\n") }
            profile.email.takeIf { it.isNotBlank() }?.let { sb.append("Email;${escape(it)}\r\n") }
            sb.append("Объект;${escape(act.title)}\r\n")
            sb.append("№;Наименование работ;Ед.изм.;Количество;Цена;Сумма\r\n")
            items.forEachIndexed { index, item ->
                sb.append("${index + 1};${escape("${item.category}: ${item.name}")};${escape(item.unitAbbr)};")
                sb.append("%.2f;%.2f;%.2f\r\n".format(item.quantity, item.price, item.total))
            }
            sb.append(";;Итого;;;%.2f\r\n".format(totalSum))
            file.writeText(sb.toString(), Charsets.UTF_8)
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun escape(s: String): String =
        if (s.contains(';') || s.contains('"') || s.contains('\n')) "\"${s.replace("\"", "\"\"")}\"" else s
}
