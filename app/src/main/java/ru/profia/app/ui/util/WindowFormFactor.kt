package ru.profia.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Класс размера экрана по ширине (коррелирует с диагональю устройства).
 * Используется для адаптивных отступов и ограничения ширины контента.
 */
enum class FormFactor {
    /** Узкий экран (&lt; 360dp) — компактные смартфоны. */
    Compact,
    /** Средняя ширина (360–599dp) — обычные смартфоны. */
    Medium,
    /** Широкий экран (≥ 600dp) — планшеты и раскладки. */
    Expanded
}

/** Минимальная ширина (dp) для класса Medium. */
private const val MEDIUM_MIN_WIDTH_DP = 360
/** Минимальная ширина (dp) для класса Expanded (планшет). */
private const val EXPANDED_MIN_WIDTH_DP = 600

/**
 * Адаптивные отступы и ограничение ширины контента в зависимости от [FormFactor].
 */
data class AdaptivePadding(
    val horizontal: Dp,
    val vertical: Dp,
    val contentMaxWidth: Dp?
) {
    companion object {
        /** Компактный экран: меньше отступы, контент на всю ширину. */
        val Compact = AdaptivePadding(
            horizontal = 12.dp,
            vertical = 12.dp,
            contentMaxWidth = null
        )
        /** Средний экран (обычный телефон). */
        val Medium = AdaptivePadding(
            horizontal = 16.dp,
            vertical = 16.dp,
            contentMaxWidth = null
        )
        /** Расширенный (планшет): больше отступы, ограничение ширины контента. */
        val Expanded = AdaptivePadding(
            horizontal = 32.dp,
            vertical = 24.dp,
            contentMaxWidth = 840.dp
        )
        /** @deprecated Используйте [Medium]. */
        @Deprecated("Use Medium", ReplaceWith("Medium"))
        val Phone = Medium
        /** @deprecated Используйте [Expanded]. */
        @Deprecated("Use Expanded", ReplaceWith("Expanded"))
        val Tablet = Expanded
    }
}

val LocalFormFactor = compositionLocalOf { FormFactor.Medium }
val LocalAdaptivePadding = compositionLocalOf { AdaptivePadding.Medium }

/**
 * Определяет класс экрана по [LocalConfiguration].
 * Используется smallestScreenWidthDp для стабильной классификации при смене ориентации.
 */
@Composable
fun rememberFormFactor(): FormFactor {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.smallestScreenWidthDp
    return remember(widthDp) {
        when {
            widthDp >= EXPANDED_MIN_WIDTH_DP -> FormFactor.Expanded
            widthDp >= MEDIUM_MIN_WIDTH_DP -> FormFactor.Medium
            else -> FormFactor.Compact
        }
    }
}

/**
 * Отступы для текущего класса экрана.
 */
@Composable
fun rememberAdaptivePadding(): AdaptivePadding {
    val formFactor = rememberFormFactor()
    return remember(formFactor) {
        when (formFactor) {
            FormFactor.Compact -> AdaptivePadding.Compact
            FormFactor.Medium -> AdaptivePadding.Medium
            FormFactor.Expanded -> AdaptivePadding.Expanded
        }
    }
}

/**
 * Предоставляет [LocalFormFactor] и [LocalAdaptivePadding] для дерева композиции.
 * Вызывать в корне (например, в [MainActivity]). Класс экрана пересчитывается при
 * смене конфигурации (ориентация, окно на планшете).
 */
@Composable
fun ProvideWindowFormFactor(content: @Composable () -> Unit) {
    val formFactor = rememberFormFactor()
    val padding = rememberAdaptivePadding()
    CompositionLocalProvider(
        LocalFormFactor provides formFactor,
        LocalAdaptivePadding provides padding,
        content = content
    )
}
