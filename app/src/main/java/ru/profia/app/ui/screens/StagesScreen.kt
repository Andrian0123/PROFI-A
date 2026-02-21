package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.data.reference.StageItem
import ru.profia.app.data.reference.StagesReference
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.theme.Divider
import androidx.navigation.NavController

@Composable
fun StagesScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null
) {
    BaseScreen(
        navController = navController,
        title = stringResource(R.string.stages),
        showBackButton = true,
        onMenuClick = onMenuClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.stages_catalog),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.stages_catalog_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                items(StagesReference.all) { stage ->
                    StageItemCard(stage = stage)
                }
            }
        }
    }
}

@Composable
private fun StageItemCard(stage: StageItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${stage.code}. ${stringResource(stage.titleResId)}",
                style = MaterialTheme.typography.titleSmall
            )
            stage.descriptionResId?.let { descResId ->
                HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    stringResource(descResId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
