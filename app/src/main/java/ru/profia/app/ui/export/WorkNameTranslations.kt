package ru.profia.app.ui.export

import android.content.Context
import ru.profia.app.data.reference.WorksReference

/**
 * Возвращает перевод названия вида работы на язык контекста для экспорта сметы/акта.
 * Если название есть в справочнике [WorksReference], подставляется строка из ресурсов work_name_1, work_name_2, …
 * (порядок = [WorksReference.orderedUniqueWorkNames]). Иначе возвращается исходная строка.
 */
fun translateWorkName(ctx: Context, name: String): String {
    val idx = WorksReference.orderedUniqueWorkNames.indexOf(name)
    if (idx < 0) return name
    val resId = ctx.resources.getIdentifier("work_name_${idx + 1}", "string", ctx.packageName)
    return if (resId != 0) ctx.getString(resId) else name
}
