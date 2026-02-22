package ru.profia.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.navigation.NavRoutes
import ru.profia.app.ui.theme.Divider
import ru.profia.app.ui.theme.Primary
import ru.profia.app.ui.theme.TextSecondary
import ru.profia.app.ui.viewmodel.AddWorkTypesViewModel
import androidx.compose.runtime.rememberCoroutineScope

/** Категория работ для списка на экране «Добавить вид работы». */
private data class WorkCategoryItem(
    val id: String,
    val name: String,
    val count: Int = 0,
    val cost: Double = 0.0
)

private val WORK_CATEGORY_IDS = listOf(
    "ceiling" to R.string.work_category_ceiling,
    "walls" to R.string.work_category_walls,
    "floor" to R.string.work_category_floor,
    "doors" to R.string.work_category_doors,
    "windows" to R.string.work_category_windows,
    "plumbing" to R.string.work_category_plumbing,
    "electrical" to R.string.work_category_electrical,
    "ventilation" to R.string.work_category_ventilation,
    "other" to R.string.work_category_other
)

private val WORK_UNIT_RES_IDS = listOf(
    R.string.work_unit_pm,
    R.string.work_unit_sqm,
    R.string.work_unit_pcs,
    R.string.work_unit_m3,
    R.string.work_unit_ton
)

@Composable
fun AddWorkTypesScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null,
    viewModel: AddWorkTypesViewModel = hiltViewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(1) } // 0 Общие данные, 1 Виды работ, 2 Материалы
    var showCalculator by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val tabLabels = listOf(
        stringResource(R.string.add_work_tab_general),
        stringResource(R.string.add_work_tab_works),
        stringResource(R.string.add_work_tab_materials)
    )
    val categories = WORK_CATEGORY_IDS.map { (id, resId) ->
        WorkCategoryItem(id, stringResource(resId), 0, 0.0)
    }
    BaseScreen(
        navController = navController,
        title = stringResource(R.string.add_work_types_title),
        actions = {
            IconButton(onClick = { showCalculator = true }) {
                Icon(Icons.Default.Calculate, contentDescription = stringResource(R.string.content_desc_calculator))
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.content_desc_add))
            }
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.content_desc_more))
                }
            }
        }
    ) { _ ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            tabLabels.forEachIndexed { index, label ->
                val selected = selectedTab == index
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        color = if (selected) Primary else TextSecondary,
                        style = if (selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
                    )
                    if (selected) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 4.dp),
                            color = Divider,
                            thickness = 2.dp
                        )
                    }
                }
            }
        }

        if (showCalculator) {
            SimpleCalculatorDialog(onDismiss = { showCalculator = false })
        }
        if (showAddDialog) {
            AddWorkTemplateDialog(
                categoryIds = WORK_CATEGORY_IDS,
                unitResIds = WORK_UNIT_RES_IDS,
                onDismiss = { showAddDialog = false },
                onSave = { name, categoryId, unitAbbr, price ->
                    viewModel.addWorkTemplate(name, categoryId, unitAbbr, price)
                    Toast.makeText(context, context.getString(R.string.add_work_toast_template_saved), Toast.LENGTH_LONG).show()
                    showAddDialog = false
                }
            )
        }
        when (selectedTab) {
            0 -> GeneralDataContent()
            1 -> WorkCategoriesList(
                categories = categories,
                onCategoryClick = { navController.navigate(NavRoutes.workCategory(it.id)) }
            )
            2 -> MaterialsContent()
        }
    }
}

@Composable
private fun GeneralDataContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.add_work_general_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.add_work_general_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun MaterialsContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.add_work_materials_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.add_work_materials_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun WorkCategoriesList(
    categories: List<WorkCategoryItem>,
    onCategoryClick: (WorkCategoryItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(categories) { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryClick(item) }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${item.count}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = "%.2f ₽".format(item.cost),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWorkTemplateDialog(
    categoryIds: List<Pair<String, Int>>,
    unitResIds: List<Int>,
    onDismiss: () -> Unit,
    onSave: (name: String, categoryId: String, unitAbbr: String, price: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categoryIds.first().first) }
    var selectedUnitResId by remember { mutableStateOf(unitResIds.first()) }
    var priceText by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val selectedCategoryName = stringResource(categoryIds.first { it.first == selectedCategoryId }.second)
    val selectedUnitAbbr = stringResource(selectedUnitResId)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    stringResource(R.string.add_work_template_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ProfiTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.add_work_template_name_label),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )
                Text(stringResource(R.string.add_work_template_category_label), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCategoryName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        for ((id, resId) in categoryIds) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(resId)) },
                                onClick = {
                                    selectedCategoryId = id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(stringResource(R.string.add_work_template_unit_label), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { unitExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedUnitAbbr, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        for (resId in unitResIds) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(resId)) },
                                onClick = {
                                    selectedUnitResId = resId
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
                ProfiTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = stringResource(R.string.add_work_template_price_label),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    val selectedUnitStr = stringResource(selectedUnitResId)
                    RoundedButton(
                        text = stringResource(R.string.add_work_template_save_btn),
                        onClick = {
                            when {
                                name.isBlank() -> Toast.makeText(context, context.getString(R.string.add_work_validation_name), Toast.LENGTH_SHORT).show()
                                priceText.isBlank() -> Toast.makeText(context, context.getString(R.string.add_work_validation_price), Toast.LENGTH_SHORT).show()
                                priceText.replace(",", ".").toDoubleOrNull() == null -> Toast.makeText(context, context.getString(R.string.add_work_validation_price_invalid), Toast.LENGTH_SHORT).show()
                                else -> {
                                    val priceVal = priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    onSave(name.trim(), selectedCategoryId, selectedUnitStr, priceVal)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
