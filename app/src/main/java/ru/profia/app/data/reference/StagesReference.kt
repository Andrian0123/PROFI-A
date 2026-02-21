package ru.profia.app.data.reference

import ru.profia.app.R

/**
 * Справочник этапов строительства/ремонта для экрана «Этапы».
 * Заголовок и описание задаются через string res id для локализации.
 */
data class StageItem(
    val code: String,
    val titleResId: Int,
    val descriptionResId: Int?
)

object StagesReference {

    /** Все этапы (порядок типовой для ремонта/строительства). */
    val all: List<StageItem> = listOf(
        StageItem("01", R.string.stage_01_title, R.string.stage_01_desc),
        StageItem("02", R.string.stage_02_title, R.string.stage_02_desc),
        StageItem("03", R.string.stage_03_title, R.string.stage_03_desc),
        StageItem("04", R.string.stage_04_title, R.string.stage_04_desc),
        StageItem("05", R.string.stage_05_title, R.string.stage_05_desc),
        StageItem("06", R.string.stage_06_title, R.string.stage_06_desc),
        StageItem("07", R.string.stage_07_title, R.string.stage_07_desc),
        StageItem("08", R.string.stage_08_title, R.string.stage_08_desc),
        StageItem("09", R.string.stage_09_title, R.string.stage_09_desc),
        StageItem("10", R.string.stage_10_title, R.string.stage_10_desc),
        StageItem("11", R.string.stage_11_title, R.string.stage_11_desc),
        StageItem("12", R.string.stage_12_title, R.string.stage_12_desc),
        StageItem("13", R.string.stage_13_title, R.string.stage_13_desc)
    )
}
