package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.data.reference.MaterialsReference
import ru.profia.app.data.reference.MaterialGroup
import ru.profia.app.data.reference.MaterialItem
import ru.profia.app.ui.components.BaseScreen
import androidx.navigation.NavController
import ru.profia.app.ui.theme.Divider

@Composable
fun MaterialsScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null
) {
    BaseScreen(
        navController = navController,
        title = stringResource(R.string.materials),
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
                stringResource(R.string.materials_catalog),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.materials_avg_prices_hint),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            MaterialsReference.groups.forEach { group ->
                MaterialGroupCard(group = group)
            }
        }
    }
}

@Composable
private fun MaterialGroupCard(group: MaterialGroup) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                group.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HorizontalDivider(color = Divider, modifier = Modifier.padding(bottom = 8.dp))
            group.items.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                MaterialItemRow(item = item)
            }
        }
    }
}

@Composable
private fun MaterialItemRow(item: MaterialItem) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            item.name,
            style = MaterialTheme.typography.bodyMedium
        )
        val priceText = when {
            item.priceMin > 0 || item.priceMax > 0 -> {
                val range = if (item.priceMin == item.priceMax)
                    "около ${item.priceMin} ₽/$item.unit"
                else
                    "от ${item.priceMin} до ${item.priceMax} ₽/$item.unit"
                if (item.note != null) "$range • ${item.note}" else range
            }
            item.note != null -> item.note!!
            else -> ""
        }
        if (priceText.isNotEmpty()) {
            Text(
                priceText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
