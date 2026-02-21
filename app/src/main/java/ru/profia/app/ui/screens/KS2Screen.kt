package ru.profia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.data.model.KS2Act
import ru.profia.app.data.model.KS2Item
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.viewmodel.ProjectTitleViewModel
import ru.profia.app.ui.theme.Divider
import ru.profia.app.ui.theme.ErrorRed
import ru.profia.app.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KS2Screen(
    navController: NavController,
    projectId: String,
    projectTitleViewModel: ProjectTitleViewModel = hiltViewModel()
) {
    val projectName by projectTitleViewModel.projectName.collectAsState()
    var ks2Act by remember {
        mutableStateOf(KS2Act(projectId = projectId))
    }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<KS2Item?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    BaseScreen(
        navController = navController,
        title = projectName ?: "Акт КС-2",
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = Primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.content_desc_add_work))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Унифицированная форма № КС-2",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Акт о приемке выполненных работ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Номер документа: ${ks2Act.number}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Дата: ${dateFormat.format(ks2Act.date)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "Отчетный период: ${dateFormat.format(ks2Act.reportPeriodStart)} - ${dateFormat.format(ks2Act.reportPeriodEnd)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ks2Act.items, key = { it.id }) { item ->
                    KS2ItemCard(
                        item = item,
                        onEdit = { editingItem = item },
                        onDelete = {
                            ks2Act = ks2Act.copy(
                                items = ks2Act.items.filter { it.id != item.id }.toMutableList()
                            )
                        }
                    )
                }
            }

            val totalSum = ks2Act.items.sumOf { it.totalPrice }
            val vatSum = totalSum * ks2Act.vatRate / 100
            val totalWithVat = totalSum + vatSum

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ИТОГО:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(
                            text = "%,.2f ₽".format(totalSum),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("НДС (${ks2Act.vatRate.toInt()}%):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "%,.2f ₽".format(vatSum),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ВСЕГО к оплате:", color = Primary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "%,.2f ₽".format(totalWithVat),
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showAddItemDialog || editingItem != null) {
        KS2ItemDialog(
            item = editingItem,
            onDismiss = {
                showAddItemDialog = false
                editingItem = null
            },
            onSave = { item ->
                if (editingItem != null) {
                    val index = ks2Act.items.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        ks2Act.items[index] = item
                    }
                } else {
                    ks2Act.items.add(item)
                }
                ks2Act = ks2Act.copy(items = ks2Act.items)
                showAddItemDialog = false
                editingItem = null
            }
        )
    }
}

@Composable
fun KS2ItemCard(
    item: KS2Item,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#${item.positionNumber}. ${item.workName}",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.quantity} ${item.unit} × %,.0f ₽".format(item.unitPrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%,.0f ₽".format(item.totalPrice),
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            if (item.unitPriceCode.isNotBlank()) {
                Text(
                    text = "Шифр: ${item.unitPriceCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun KS2ItemDialog(
    item: KS2Item?,
    onDismiss: () -> Unit,
    onSave: (KS2Item) -> Unit
) {
    var positionNumber by remember { mutableStateOf((item?.positionNumber ?: 1).toString()) }
    var workName by remember { mutableStateOf(item?.workName ?: "") }
    var unit by remember { mutableStateOf((item?.unit).orEmpty().ifEmpty { "м²" }) }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "") }
    var unitPrice by remember { mutableStateOf(item?.unitPrice?.toString() ?: "") }
    var unitPriceCode by remember { mutableStateOf(item?.unitPriceCode ?: "") }
    val units = listOf("м²", "м³", "шт", "компл", "т", "кг", "п.м.")

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (item == null) "Добавить работу" else "Редактировать работу",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ProfiTextField(
                    value = positionNumber,
                    onValueChange = { positionNumber = it },
                    label = "№ п/п",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                ProfiTextField(
                    value = workName,
                    onValueChange = { workName = it },
                    label = "Наименование работ",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                ProfiTextField(
                    value = unitPriceCode,
                    onValueChange = { unitPriceCode = it },
                    label = "Шифр расценки",
                    placeholder = { Text("ФЕР 12-01-007-07") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Text("Ед. изм.", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (u in units) {
                        androidx.compose.material3.FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfiTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = "Количество",
                        modifier = Modifier.weight(1f)
                    )
                    ProfiTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = "Цена за ед.",
                        modifier = Modifier.weight(1f)
                    )
                }
                val quantityVal = quantity.replace(",", ".").toDoubleOrNull() ?: 0.0
                val priceVal = unitPrice.replace(",", ".").toDoubleOrNull() ?: 0.0
                val total = quantityVal * priceVal
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Сумма:")
                        Text(
                            text = "%,.2f ₽".format(total),
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Button(
                        onClick = {
                            val newItem = KS2Item(
                                id = item?.id ?: UUID.randomUUID().toString(),
                                positionNumber = positionNumber.toIntOrNull() ?: 1,
                                estimatePosition = positionNumber,
                                workName = workName,
                                unitPriceCode = unitPriceCode,
                                unit = unit,
                                quantity = quantityVal,
                                unitPrice = priceVal,
                                totalPrice = total
                            )
                            onSave(newItem)
                        },
                        enabled = workName.isNotBlank() && quantity.isNotBlank() && unitPrice.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}
