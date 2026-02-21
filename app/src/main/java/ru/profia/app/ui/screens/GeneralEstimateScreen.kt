package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.ui.graphics.Color
import ru.profia.app.R
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import android.content.Intent
import android.widget.Toast
import ru.profia.app.ui.export.EstimateExport
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.data.local.entity.RoomWorkItemEntity
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.data.model.ProjectData
import ru.profia.app.data.model.UserProfile
import ru.profia.app.ui.viewmodel.EstimateItemUi
import ru.profia.app.ui.viewmodel.EstimateRoomSection
import ru.profia.app.ui.viewmodel.GeneralEstimateViewModel
import java.util.Locale

@Composable
fun GeneralEstimateScreen(
    navController: NavController,
    projectId: String,
    isFinalEstimate: Boolean = false,
    viewModel: GeneralEstimateViewModel = hiltViewModel()
) {
    val sections by viewModel.sections.collectAsState()
    val totalSum by viewModel.totalSum.collectAsState()
    val workItemIdsInActs by viewModel.workItemIdsInActs.collectAsState()
    val project by viewModel.project.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val userAccountType by viewModel.userAccountType.collectAsState()
    var expandedMenuId by remember { mutableStateOf<String?>(null) }
    var editDialogItem by remember { mutableStateOf<EstimateItemUi?>(null) }
    var shareMenuExpanded by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateSetOf<String>() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val estimateLanguageOptions = remember {
        listOf(
            Triple(Locale.forLanguageTag("ru"), R.string.estimate_language_ru, R.string.estimate_language_ru_short),
            Triple(Locale.forLanguageTag("en"), R.string.estimate_language_en, R.string.estimate_language_en_short),
            Triple(Locale.forLanguageTag("de"), R.string.estimate_language_de, R.string.estimate_language_de_short),
            Triple(Locale.forLanguageTag("ro"), R.string.estimate_language_ro, R.string.estimate_language_ro_short)
        )
    }
    var selectedExportLocale by remember { mutableStateOf(Locale.forLanguageTag("ru")) }
    var estimateLangMenuExpanded by remember { mutableStateOf(false) }
    val titleEstimate = if (isFinalEstimate) stringResource(R.string.final_estimate) else stringResource(R.string.preliminary_estimate)
    val subtitleEstimate = if (isFinalEstimate) stringResource(R.string.final_estimate_subtitle) else stringResource(R.string.preliminary_estimate_subtitle)

    LaunchedEffect(projectId) {
        viewModel.loadEstimate()
    }
    editDialogItem?.let { ui ->
        EditWorkItemDialog(
            item = ui.item,
            onDismiss = { editDialogItem = null },
            onSave = { updated ->
                viewModel.updateWorkItem(updated)
                editDialogItem = null
            }
        )
    }

    fun shareFile(file: java.io.File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }

    BaseScreen(
        navController = navController,
        title = project?.displayName ?: titleEstimate,
        showBackButton = true,
        onBackClick = if (selectionMode) { { selectionMode = false; selectedIds.clear() } } else null,
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val langShort = estimateLanguageOptions
                    .firstOrNull { it.first.language == selectedExportLocale.language }
                    ?.third
                    ?.let { stringResource(it) }
                    ?: selectedExportLocale.language.uppercase().take(2)
                TextButton(onClick = { estimateLangMenuExpanded = true }) {
                    Text(langShort, style = MaterialTheme.typography.bodySmall)
                }
                DropdownMenu(
                    expanded = estimateLangMenuExpanded,
                    onDismissRequest = { estimateLangMenuExpanded = false }
                ) {
                    estimateLanguageOptions.forEach { (locale, labelResId, _) ->
                        DropdownMenuItem(
                            text = { Text(stringResource(labelResId)) },
                            onClick = {
                                selectedExportLocale = locale
                                estimateLangMenuExpanded = false
                            }
                        )
                    }
                }
                Box {
                    IconButton(
                        onClick = { shareMenuExpanded = true },
                        enabled = sections.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_estimate),
                            tint = if (sections.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                    DropdownMenu(
                        expanded = shareMenuExpanded,
                        onDismissRequest = { shareMenuExpanded = false }
                    ) {
                        val executorLabel = stringResource(R.string.estimate_contractor)
                        DropdownMenuItem(
                            text = { Text("PDF") },
                            onClick = {
                                shareMenuExpanded = false
                                scope.launch {
                                    val prefix = if (isFinalEstimate) "itog_smeta" else "smeta"
                                    val customerLines = buildCustomerLines(project)
                                    val executorLines = buildExecutorLines(userProfile, userAccountType, executorLabel)
                                    val titleResId = if (isFinalEstimate) R.string.final_estimate else R.string.preliminary_estimate
                                    val file = withContext(Dispatchers.IO) {
                                        EstimateExport.exportToPdf(
                                            context, sections, totalSum,
                                            title = titleEstimate, filePrefix = prefix,
                                            customerLines = customerLines, executorLines = executorLines,
                                            exportLocale = selectedExportLocale, titleResId = titleResId
                                        )
                                    }
                                    if (file != null) shareFile(file, "application/pdf")
                                    else Toast.makeText(context, context.getString(R.string.export_pdf_failed), Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Excel (CSV)") },
                            onClick = {
                                shareMenuExpanded = false
                                scope.launch {
                                    val prefix = if (isFinalEstimate) "itog_smeta" else "smeta"
                                    val customerLines = buildCustomerLines(project)
                                    val executorLines = buildExecutorLines(userProfile, userAccountType, executorLabel)
                                    val titleResId = if (isFinalEstimate) R.string.final_estimate else R.string.preliminary_estimate
                                    val file = withContext(Dispatchers.IO) {
                                        EstimateExport.exportToCsv(
                                            context, sections, totalSum,
                                            title = titleEstimate, filePrefix = prefix,
                                            customerLines = customerLines, executorLines = executorLines,
                                            exportLocale = selectedExportLocale, titleResId = titleResId
                                        )
                                    }
                                    if (file != null) shareFile(file, "text/csv")
                                    else Toast.makeText(context, context.getString(R.string.export_csv_failed), Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!isFinalEstimate) {
                if (selectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    TextButton(onClick = { selectionMode = false; selectedIds.clear() }) {
                        Text(stringResource(R.string.cancel))
                    }
                    RoundedButton(
                        text = stringResource(R.string.create),
                            onClick = {
                                val selected = sections.flatMap { it.items }.filter { it.item.id in selectedIds }
                                viewModel.saveIntermediateEstimateAct(selected)
                                selectionMode = false
                                selectedIds.clear()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = selectedIds.isNotEmpty()
                        )
                    }
                    Text(
                        stringResource(R.string.select_works_for_intermediate),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoundedButton(
                            text = stringResource(R.string.create_intermediate_estimate),
                            onClick = { selectionMode = true },
                            modifier = Modifier.weight(1f),
                            enabled = sections.isNotEmpty()
                        )
                    }
                }
            }
            Text(
                subtitleEstimate,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (sections.isEmpty()) {
                Text(
                    stringResource(R.string.no_works_add_in_rooms),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sections.forEach { section ->
                        item(key = "header_${section.roomName}") {
                            Text(
                                section.roomName,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(
                            items = section.items,
                            key = { it.item.id }
                        ) { ui ->
                            val item = ui.item
                            val isInAct = item.id in workItemIdsInActs
                            val isSelected = selectionMode && item.id in selectedIds
                            val canSelect = selectionMode && !isInAct
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        when {
                                            isSelected -> Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                            isInAct && selectionMode -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            else -> Modifier
                                        }
                                    ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    if (selectionMode) {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(32.dp)
                                                .then(
                                                    if (canSelect) Modifier.clickable {
                                                        if (item.id in selectedIds) selectedIds.remove(item.id)
                                                        else selectedIds.add(item.id)
                                                    }
                                                    else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isInAct) {
                                                Text("—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                            } else {
                                                Icon(
                                                    imageVector = if (item.id in selectedIds) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = if (item.id in selectedIds) "Выбрано" else "Выбрать",
                                                    modifier = Modifier.size(28.dp),
                                                    tint = if (item.id in selectedIds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = "${item.category}: ${item.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 3
                                        )
                                        Text(
                                            "${"%.2f".format(item.quantity)} ${item.unitAbbr}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            "Цена: ${"%.2f".format(item.price)} ₽",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.widthIn(max = 110.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            "${"%.2f".format(item.total)} ₽",
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                        if (!selectionMode && isInAct) {
                                            Text(
                                                text = stringResource(R.string.in_act_edit_in_acts),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                    if (!selectionMode && !isInAct) {
                                                Box {
                                                    IconButton(
                                                        onClick = { expandedMenuId = if (expandedMenuId == item.id) null else item.id },
                                                        modifier = Modifier.size(40.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.MoreVert,
                                                            contentDescription = stringResource(R.string.content_desc_menu)
                                                        )
                                                    }
                                                    DropdownMenu(
                                                        expanded = expandedMenuId == item.id,
                                                        onDismissRequest = { expandedMenuId = null }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.edit_action)) },
                                                            onClick = {
                                                                expandedMenuId = null
                                                                editDialogItem = ui
                                                            },
                                                            leadingIcon = {
                                                                Icon(Icons.Default.Edit, contentDescription = null)
                                                            }
                                                        )
                                                    DropdownMenuItem(
                                                        text = { Text(stringResource(R.string.delete_action)) },
                                                        onClick = {
                                                            expandedMenuId = null
                                                            viewModel.deleteWorkItem(item.id)
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.Delete, contentDescription = null)
                                                        }
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                        }
                        item(key = "total_${section.roomName}") {
                            val sectionSum = section.items.sumOf { it.item.total }
                            Text(
                                "Итого: ${"%.2f".format(sectionSum)} ₽",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Итого стоимость работы",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            "${"%.2f".format(totalSum)} ₽",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }

    }
}

internal fun buildCustomerLines(project: ProjectData?): List<String>? {
    if (project == null) return null
    val lines = mutableListOf<String>()
    lines.add("Заказчик")
    val fio = listOf(project.lastName, project.firstName, project.middleName.orEmpty()).filter { it.isNotBlank() }.joinToString(" ")
    if (fio.isNotBlank()) lines.add("ФИО: $fio")
    val addr = project.address.takeIf { it.isNotBlank() }
        ?: listOf(project.city, project.street, project.house, project.apartment).filterNotNull().filter { it.isNotBlank() }.joinToString(", ")
    if (addr.isNotBlank()) lines.add("Адрес: $addr")
    project.phone?.takeIf { it.isNotBlank() }?.let { lines.add("Тел.: $it") }
    project.email?.takeIf { it.isNotBlank() }?.let { lines.add("Email: $it") }
    return if (lines.size <= 1) null else lines
}

/**
 * Строки блока «Подрядчик» (исполнитель) для экспорта.
 * @param executorLabel Заголовок блока (например из R.string.estimate_contractor).
 * @param accountType PROFI — только профиль (ФИО, тел., email); BUSINESS — профиль + компания + реквизиты.
 */
internal fun buildExecutorLines(profile: UserProfile?, accountType: String?, executorLabel: String = "Подрядчик"): List<String>? {
    if (profile == null) return null
    val lines = mutableListOf<String>()
    lines.add(executorLabel)
    val isProfi = accountType == "PROFI"
    val name = if (isProfi) {
        listOf(profile.lastName, profile.firstName, profile.middleName.orEmpty()).filter { it.isNotBlank() }.joinToString(" ")
    } else {
        profile.companyName?.takeIf { it.isNotBlank() }
            ?: listOf(profile.lastName, profile.firstName, profile.middleName.orEmpty()).filter { it.isNotBlank() }.joinToString(" ")
    }
    if (name.isNotBlank()) lines.add(name)
    if (!isProfi) {
        profile.inn?.takeIf { it.isNotBlank() }?.let { lines.add("ИНН: $it") }
        profile.kpp?.takeIf { it.isNotBlank() }?.let { lines.add("КПП: $it") }
        profile.legalAddress?.takeIf { it.isNotBlank() }?.let { lines.add("Адрес: $it") }
        profile.bankName?.takeIf { it.isNotBlank() }?.let { lines.add("Банк: $it") }
        profile.accountNumber?.takeIf { it.isNotBlank() }?.let { lines.add("Р/с: $it") }
        profile.correspondentAccount?.takeIf { it.isNotBlank() }?.let { lines.add("К/с: $it") }
        profile.bic?.takeIf { it.isNotBlank() }?.let { lines.add("БИК: $it") }
    }
    profile.phone.takeIf { it.isNotBlank() }?.let { lines.add("Тел.: $it") }
    profile.email.takeIf { it.isNotBlank() }?.let { lines.add("Email: $it") }
    return if (lines.size <= 1) null else lines
}

@Composable
private fun EditWorkItemDialog(
    item: RoomWorkItemEntity,
    onDismiss: () -> Unit,
    onSave: (RoomWorkItemEntity) -> Unit
) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var unitAbbr by remember(item.id) { mutableStateOf(item.unitAbbr) }
    var priceStr by remember(item.id) { mutableStateOf("%.2f".format(item.price)) }
    var quantityStr by remember(item.id) { mutableStateOf("%.2f".format(item.quantity)) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.edit_position), style = MaterialTheme.typography.titleMedium)
                ProfiTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.item_name),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                ProfiTextField(
                    value = unitAbbr,
                    onValueChange = { unitAbbr = it },
                    label = stringResource(R.string.unit_abbr),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                ProfiTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = stringResource(R.string.price_rub),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ProfiTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = stringResource(R.string.quantity),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    TextButton(
                        onClick = {
                            val price = priceStr.replace(",", ".").toDoubleOrNull() ?: item.price
                            val qty = quantityStr.replace(",", ".").toDoubleOrNull() ?: item.quantity
                            onSave(item.copy(name = name, unitAbbr = unitAbbr, price = price, quantity = qty))
                        }
                    ) { Text(stringResource(R.string.save)) }
                }
            }
        }
    }
}

