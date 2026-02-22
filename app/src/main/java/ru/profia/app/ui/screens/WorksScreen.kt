package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.data.reference.WorkGroup
import ru.profia.app.data.reference.WorkItem
import ru.profia.app.data.reference.WorkSection
import ru.profia.app.data.reference.WorksReference
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.navigation.NavRoutes
import androidx.navigation.NavController
import ru.profia.app.ui.theme.Divider

@Composable
fun WorksScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null
) {
    BaseScreen(
        navController = navController,
        title = stringResource(R.string.works),
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
                stringResource(R.string.works_catalog),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.works_avg_prices_hint),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            WorksReference.sections.forEach { section ->
                val sectionTitleResId = when (section.id) {
                    "internal" -> R.string.works_section_internal
                    "external" -> R.string.works_section_external
                    else -> null
                }
                val sectionTitle = sectionTitleResId?.let { stringResource(it) } ?: section.title
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { navController.navigate(NavRoutes.workSection(section.id)) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        sectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.works_price_disclaimer_title),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(bottom = 8.dp))
                    WorksReference.priceDisclaimerLines.forEach { line ->
                        Text(
                            "• $line",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Button(
                onClick = { navController.navigate(NavRoutes.ADD_WORK_TYPES) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.add_work_type))
            }
        }
    }
}

/**
 * Экран списка групп (папок) работ внутри секции «Внутренние» или «Наружные».
 */
@Composable
fun WorksSectionScreen(
    navController: NavController,
    sectionId: String,
    onMenuClick: (() -> Unit)? = null
) {
    val section = WorksReference.sections.find { it.id == sectionId }
    val sectionTitleResId = when (sectionId) {
        "internal" -> R.string.works_section_internal
        "external" -> R.string.works_section_external
        else -> null
    }
    val title = sectionTitleResId?.let { stringResource(it) } ?: section?.title ?: sectionId

    BaseScreen(
        navController = navController,
        title = title,
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
            if (section != null) {
                section.groups.forEach { group ->
                    WorkGroupCard(group = group)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.works_price_disclaimer_title),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(bottom = 8.dp))
                    WorksReference.priceDisclaimerLines.forEach { line ->
                        Text(
                            "• $line",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkGroupCard(group: WorkGroup) {
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
                WorkItemRow(item = item)
            }
        }
    }
}

@Composable
private fun WorkItemRow(item: WorkItem) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            item.name,
            style = MaterialTheme.typography.bodyMedium
        )
        val priceText = when {
            item.priceMin > 0 || item.priceMax > 0 -> {
                val range = if (item.priceMin == item.priceMax)
                    context.getString(R.string.works_price_about, item.priceMin, item.unit)
                else
                    context.getString(R.string.works_price_range, item.priceMin, item.priceMax, item.unit)
                if (item.note != null) "$range ($item.note)" else range
            }
            item.note != null -> item.note
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
