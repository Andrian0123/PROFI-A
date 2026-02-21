package ru.profia.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
/**
 * Анимации переходов между экранами.
 */
fun slideInFromRight() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

fun slideOutToLeft() = slideOutHorizontally(
    targetOffsetX = { -it / 4 },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

fun slideInFromLeft() = slideInHorizontally(
    initialOffsetX = { -it / 4 },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

fun slideOutToRight() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))
