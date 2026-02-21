package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen

@Composable
fun LegalDocumentScreen(
    navController: NavController,
    titleResId: Int,
    contentResId: Int,
    onMenuClick: (() -> Unit)? = null
) {
    BaseScreen(
        navController = navController,
        title = stringResource(titleResId),
        showBackButton = true,
        onMenuClick = onMenuClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(contentResId),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
