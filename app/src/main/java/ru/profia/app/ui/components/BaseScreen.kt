package ru.profia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.ui.theme.Background
import ru.profia.app.ui.theme.OnPrimary
import ru.profia.app.ui.theme.Primary
import ru.profia.app.ui.util.LocalAdaptivePadding

/**
 * Базовый экран с верхней панелью (TopAppBar): заголовок, кнопки меню/назад и actions.
 * Шапка зелёная (brand), фон контента — светлый.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BaseScreen(
    navController: NavController? = null,
    title: String? = null,
    showBackButton: Boolean = true,
    showSaveButton: Boolean = false,
    saveAsCheckIcon: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    isSaveEnabled: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    useAdaptivePadding: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val backAction = onBackClick ?: { navController?.popBackStack() }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(76.dp),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        LogoSquare(size = 28.dp, showLetter = false)
                        Text(
                            text = "ПРОФЙ-А",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnPrimary,
                            modifier = Modifier.padding(start = 6.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onMenuClick != null) {
                            IconButton(
                                onClick = onMenuClick,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.menu),
                                    tint = OnPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        if (showBackButton) {
                            IconButton(
                                onClick = { backAction() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                    tint = OnPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (showSaveButton && onSave != null) {
                        IconButton(
                            onClick = onSave,
                            enabled = isSaveEnabled,
                            modifier = Modifier.size(48.dp).padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (saveAsCheckIcon) Icons.Default.Check else Icons.Default.Save,
                                contentDescription = stringResource(R.string.save),
                                tint = if (isSaveEnabled) OnPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    actions()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary,
                    actionIconContentColor = OnPrimary
                )
            )
        },
        containerColor = Background,
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        val adaptive = LocalAdaptivePadding.current
        val horizontal = if (useAdaptivePadding) adaptive.horizontal else 0.dp
        val vertical = if (useAdaptivePadding) adaptive.vertical else 0.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(horizontal = horizontal, vertical = vertical)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        adaptive.contentMaxWidth?.takeIf { useAdaptivePadding }?.let { maxW ->
                            Modifier.widthIn(max = maxW).align(Alignment.Center)
                        } ?: Modifier.fillMaxWidth()
                    )
            ) {
                content(paddingValues)
            }
        }
    }
}
