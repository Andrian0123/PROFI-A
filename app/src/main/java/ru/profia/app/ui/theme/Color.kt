package ru.profia.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Single source-of-truth for UI colors.
 * PROFI-A brand: dark olive + dark green. XML (colors.xml, themes.xml) must stay in sync.
 */
object BrandColorTokens {
    val DarkOlive = Color(0xFF2F3B27)
    val DarkGreen = Color(0xFF1F2A1B)
    val WarmBeige = Color(0xFFD9C5A0)
    val WarmBackground = Color(0xFFFFF8E7)
    val AccentBrown = Color(0xFFA67C52)
}

// ----- Canonical aliases used by the current codebase -----
val OliveBranch = BrandColorTokens.DarkOlive
val OliveDeep = BrandColorTokens.DarkGreen
val Burlap = BrandColorTokens.WarmBeige
val CoconutFlour = BrandColorTokens.WarmBackground
val CinnamonStick = BrandColorTokens.AccentBrown

// Текст и разделители
val TextPrimary = Color(0xFF2C2C2C)
val TextSecondary = Color(0xFF6B6B6B)
val Divider = Color(0xFFE0D9D0)
val White = Color(0xFFFFFFFF)
val ErrorRed = Color(0xFFD32F2F)

// Назначение цветов для компонентов (все непрозрачные для читаемости)
val Primary = OliveBranch
val PrimaryVariant = OliveDeep
val Secondary = Burlap
val Background = CoconutFlour
/** Непрозрачный фон карточек и поверхностей (светлый беж в тон палитре). */
val Surface = Color(0xFFF5EFE0)
/** Непрозрачный вариант поверхности (чуть темнее Surface). */
val SurfaceVariant = Color(0xFFEDE6D8)
val OnPrimary = White
val OnBackground = TextPrimary
val OnSurface = TextPrimary

// Дополнительные совместимые алиасы (backward compatibility)
val Khaki = OliveBranch
val KhakiDark = PrimaryVariant
val KhakiLight = OliveBranch.copy(alpha = 0.5f)
val MilkyBackground = CoconutFlour
val SurfaceWhite = White
val DividerBeige = Divider
val Pistachio = OliveBranch
val PistachioDark = PrimaryVariant
val BeigeBackground = CoconutFlour
val BeigeSurface = Surface
val CinnamonBrown = CinnamonStick
val MetallicSilver = White
val SilverGreyLight = Divider
