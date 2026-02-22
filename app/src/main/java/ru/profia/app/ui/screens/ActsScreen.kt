package ru.profia.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ru.profia.app.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.EmptyState
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.theme.Divider
import ru.profia.app.ui.theme.Primary
import ru.profia.app.ui.theme.TextPrimary
import ru.profia.app.ui.export.EstimateExport
import ru.profia.app.ui.navigation.NavRoutes
import ru.profia.app.ui.viewmodel.ActsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActsScreen(
    navController: NavController,
    projectId: String,
    viewModel: ActsViewModel = hiltViewModel()
) {
    val acts by viewModel.acts.collectAsState()
    val actItems by viewModel.actItems.collectAsState()
    val project by viewModel.project.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val userAccountType by viewModel.userAccountType.collectAsState()
    val context = LocalContext.current
    val discountDefaultStr = stringResource(R.string.discount_variant_default)
    val taxDefaultStr = stringResource(R.string.tax_variant_default)
    val discount5Str = stringResource(R.string.acts_discount_5)
    val discount10Str = stringResource(R.string.acts_discount_10)
    val taxVat20Str = stringResource(R.string.acts_tax_vat_20)
    val discountCustomStr = stringResource(R.string.discount_custom_option)
    val taxCustomStr = stringResource(R.string.tax_custom_option)
    var selectedAct by remember { mutableStateOf<IntermediateEstimateActEntity?>(null) }
    var discountMenuExpanded by remember { mutableStateOf(false) }
    var taxMenuExpanded by remember { mutableStateOf(false) }
    var estimateLangMenuExpanded by remember { mutableStateOf(false) }
    val estimateLanguageOptions = remember {
        listOf(
            Triple(Locale.forLanguageTag("ru"), R.string.estimate_language_ru, R.string.estimate_language_ru_short),
            Triple(Locale.forLanguageTag("en"), R.string.estimate_language_en, R.string.estimate_language_en_short),
            Triple(Locale.forLanguageTag("de"), R.string.estimate_language_de, R.string.estimate_language_de_short),
            Triple(Locale.forLanguageTag("ro"), R.string.estimate_language_ro, R.string.estimate_language_ro_short)
        )
    }
    var selectedEstimateLocale by remember { mutableStateOf(Locale.forLanguageTag("ru")) }
    var selectedDiscount by remember { mutableStateOf(context.getString(R.string.discount_variant_default)) }
    var selectedTax by remember { mutableStateOf(context.getString(R.string.tax_variant_default)) }
    var customDiscountPercent by remember { mutableStateOf("") }
    var customTaxPercent by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var showExportDisclaimerDialog by remember { mutableStateOf(false) }
    var pendingExportAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (showExportDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showExportDisclaimerDialog = false; pendingExportAction = null },
            title = { Text(stringResource(R.string.export_disclaimer_dialog_title)) },
            text = { Text(stringResource(R.string.export_disclaimer_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { viewModel.markExportDisclaimerSeen() }
                        showExportDisclaimerDialog = false
                        pendingExportAction?.invoke()
                        pendingExportAction = null
                    }
                ) { Text(stringResource(R.string.export_disclaimer_dialog_ok)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch { viewModel.markExportDisclaimerSeen() }
                        showExportDisclaimerDialog = false
                        pendingExportAction = null
                        navController.navigate(NavRoutes.ABOUT) { launchSingleTop = true }
                    }
                ) { Text(stringResource(R.string.export_disclaimer_dialog_more)) }
            }
        )
    }

    LaunchedEffect(projectId) {
        viewModel.loadActs()
    }
    LaunchedEffect(selectedAct) {
        selectedAct?.let { viewModel.loadActItems(it.id) }
        if (selectedAct == null) viewModel.clearActItems()
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

    fun discountDisplayText(): String? = when {
        selectedDiscount == discountDefaultStr -> null
        selectedDiscount == discountCustomStr -> customDiscountPercent.takeIf { it.isNotBlank() }?.let { "$it%" }
        else -> selectedDiscount
    }
    fun taxDisplayText(): String? = when {
        selectedTax == taxDefaultStr -> null
        selectedTax == taxCustomStr -> customTaxPercent.takeIf { it.isNotBlank() }?.let { "$it%" }
        else -> selectedTax
    }

    val executorLabel = stringResource(R.string.estimate_contractor)
    selectedAct?.let { act ->
        ActContentDialog(
            act = act,
            items = actItems,
            discountPercentText = discountDisplayText(),
            taxPercentText = taxDisplayText(),
            viewModel = viewModel,
            onDismiss = { selectedAct = null },
            onSharePdf = {
                val doExport: () -> Unit = {
                    val customerLines = buildCustomerLines(project)
                    val executorLines = buildExecutorLines(userProfile, userAccountType, executorLabel)
                    val total = actItems.sumOf { it.total }
                    val discountText = discountDisplayText()
                    val taxText = taxDisplayText()
                    scope.launch {
                        val file = withContext(Dispatchers.IO) {
                            EstimateExport.exportActToPdf(
                                context, act.title, actItems, total,
                                customerLines = customerLines, executorLines = executorLines,
                                discountText = discountText, taxPercentText = taxText,
                                exportLocale = selectedEstimateLocale
                            )
                        }
                        if (file != null) shareFile(file, "application/pdf")
                        else Toast.makeText(context, context.getString(R.string.export_pdf_failed), Toast.LENGTH_LONG).show()
                    }
                    Unit
                }
                scope.launch {
                    if (viewModel.isExportDisclaimerSeen()) doExport()
                    else { showExportDisclaimerDialog = true; pendingExportAction = doExport }
                }
            },
            onShareExcel = {
                val doExport: () -> Unit = {
                    val customerLines = buildCustomerLines(project)
                    val executorLines = buildExecutorLines(userProfile, userAccountType, executorLabel)
                    val total = actItems.sumOf { it.total }
                    val discountText = discountDisplayText()
                    val taxText = taxDisplayText()
                    scope.launch {
                        val file = withContext(Dispatchers.IO) {
                            EstimateExport.exportActToCsv(
                                context, act.title, actItems, total,
                                customerLines = customerLines, executorLines = executorLines,
                                discountText = discountText, taxPercentText = taxText,
                                exportLocale = selectedEstimateLocale
                            )
                        }
                        if (file != null) shareFile(file, "text/csv")
                        else Toast.makeText(context, context.getString(R.string.export_csv_failed), Toast.LENGTH_LONG).show()
                    }
                    Unit
                }
                scope.launch {
                    if (viewModel.isExportDisclaimerSeen()) doExport()
                    else { showExportDisclaimerDialog = true; pendingExportAction = doExport }
                }
            }
        )
    }

    BaseScreen(
        navController = navController,
        title = project?.displayName ?: stringResource(R.string.acts),
        showBackButton = true,
        modifier = Modifier.testTag("acts_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.discount_label),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            TextButton(onClick = { discountMenuExpanded = true }) {
                                Text(
                                    text = if (selectedDiscount == discountDefaultStr) stringResource(R.string.discount_label_short) else "${stringResource(R.string.discount_label_short)}: ${if (selectedDiscount == discountCustomStr) (customDiscountPercent.ifEmpty { "…" } + "%") else selectedDiscount}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            DropdownMenu(
                                expanded = discountMenuExpanded,
                                onDismissRequest = { discountMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("—") },
                                    onClick = {
                                        selectedDiscount = discountDefaultStr
                                        selectedTax = taxDefaultStr
                                        customDiscountPercent = ""
                                        customTaxPercent = ""
                                        discountMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(discount5Str) },
                                    onClick = {
                                        selectedDiscount = discount5Str
                                        selectedTax = taxDefaultStr
                                        customTaxPercent = ""
                                        discountMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(discount10Str) },
                                    onClick = {
                                        selectedDiscount = discount10Str
                                        selectedTax = taxDefaultStr
                                        customTaxPercent = ""
                                        discountMenuExpanded = false
                                    }
                                )
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                                    ProfiTextField(
                                        value = customDiscountPercent,
                                        onValueChange = {
                                            customDiscountPercent = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
                                            if (customDiscountPercent.isNotBlank()) {
                                                selectedDiscount = discountCustomStr
                                                selectedTax = taxDefaultStr
                                                customTaxPercent = ""
                                            }
                                        },
                                        label = stringResource(R.string.discount_percent_hint),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }
                            }
                        }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.tax_label_ip),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    TextButton(onClick = { taxMenuExpanded = true }) {
                        Text(
                            text = if (selectedTax == taxDefaultStr) stringResource(R.string.tax_label_ip) else "${stringResource(R.string.tax_label_ip)}: ${if (selectedTax == taxCustomStr) (customTaxPercent.ifEmpty { "…" } + "%") else selectedTax}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    DropdownMenu(
                        expanded = taxMenuExpanded,
                        onDismissRequest = { taxMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("—") },
                            onClick = {
                                selectedTax = taxDefaultStr
                                selectedDiscount = discountDefaultStr
                                customDiscountPercent = ""
                                customTaxPercent = ""
                                taxMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(taxVat20Str) },
                            onClick = {
                                selectedTax = taxVat20Str
                                selectedDiscount = discountDefaultStr
                                customDiscountPercent = ""
                                taxMenuExpanded = false
                            }
                        )
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                            ProfiTextField(
                                value = customTaxPercent,
                                onValueChange = {
                                    customTaxPercent = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
                                    if (customTaxPercent.isNotBlank()) {
                                        selectedTax = taxCustomStr
                                        selectedDiscount = discountDefaultStr
                                        customDiscountPercent = ""
                                    }
                                },
                                label = stringResource(R.string.tax_percent_hint),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Кнопка языка сметы: показываем действующий язык (Ру, En, De, Ro)
                    val langShort = estimateLanguageOptions
                        .firstOrNull { it.first.language == selectedEstimateLocale.language }
                        ?.third
                        ?.let { stringResource(it) }
                        ?: selectedEstimateLocale.language.uppercase().take(2)
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
                                    selectedEstimateLocale = locale
                                    estimateLangMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
                    Text(
                        stringResource(R.string.saved_intermediate_estimates),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (acts.isEmpty()) {
                        EmptyState(
                            title = stringResource(R.string.empty_state_title),
                            subtitle = stringResource(R.string.no_acts_create_intermediate),
                            action = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            items(acts, key = { it.id }) { act ->
                ActCard(
                    act = act,
                    onClick = { selectedAct = act },
                    onDelete = {
                        viewModel.deleteAct(act.id)
                        if (selectedAct?.id == act.id) selectedAct = null
                    }
                )
            }
        }
    }
}

@Composable
private fun ActCard(
    act: IntermediateEstimateActEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(act.createdAt))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.intermediate_estimate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_action)
                )
            }
        }
    }
}

/** Извлекает процент из строк вида "5%", "10%", "НДС 20%" и т.п. */
private fun parsePercentFromDisplayText(text: String?): Double? = text?.let { s ->
    val numStr = Regex("""(\d+([.,]\d+)?)""").find(s)?.groupValues?.get(1)?.replace(",", ".")
    numStr?.toDoubleOrNull()
}?.takeIf { it in 0.0..100.0 }

@Composable
private fun ActContentDialog(
    act: IntermediateEstimateActEntity,
    items: List<IntermediateEstimateActItemEntity>,
    discountPercentText: String? = null,
    taxPercentText: String? = null,
    viewModel: ActsViewModel,
    onDismiss: () -> Unit,
    onSharePdf: () -> Unit,
    onShareExcel: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(act.createdAt))
    var runningTotal = items.sumOf { it.total }
    val discountPercent = parsePercentFromDisplayText(discountPercentText)
    val discountAmount = if (discountPercent != null && discountPercent > 0) runningTotal * (discountPercent / 100.0) else null
    if (discountAmount != null) runningTotal -= discountAmount
    val taxPercent = parsePercentFromDisplayText(taxPercentText)
    val taxAmount = if (taxPercent != null && taxPercent > 0) runningTotal * (taxPercent / 100.0) else null
    if (taxAmount != null) runningTotal += taxAmount
    val showFinalTotal = (discountPercent != null && discountPercent > 0) || (taxPercent != null && taxPercent > 0)
    val scrollState = rememberScrollState()
    var editItem by remember { mutableStateOf<IntermediateEstimateActItemEntity?>(null) }
    var shareMenuExpanded by remember { mutableStateOf(false) }

    editItem?.let { item ->
        EditActItemDialog(
            item = item,
            onDismiss = { editItem = null },
            onSave = { updated ->
                viewModel.updateActItem(updated)
                editItem = null
            }
        )
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.intermediate_estimate),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.groupBy { it.roomName }.forEach { (roomName, roomItems) ->
                        Text(roomName, style = MaterialTheme.typography.titleSmall)
                        roomItems.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${item.category}: ${item.name}", style = MaterialTheme.typography.bodyMedium)
                                        Text("${"%.2f".format(item.quantity)} ${item.unitAbbr} × ${"%.2f".format(item.price)} ₽ = ${"%.2f".format(item.total)} ₽", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(
                                        onClick = { editItem = item },
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_action))
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    "${stringResource(R.string.estimate_total_works_label)}: ${"%.2f".format(items.sumOf { it.total })} ₽",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (discountAmount != null && discountPercent != null) {
                    Text(
                        "${stringResource(R.string.estimate_discount_amount_label)} ${"%.2f".format(discountAmount)} ₽",
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (taxAmount != null && taxPercent != null) {
                    Text(
                        "${stringResource(R.string.estimate_tax_amount_label, taxPercent.toInt().toString())} ${"%.2f".format(taxAmount)} ₽",
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (showFinalTotal) {
                    Text(
                        "${stringResource(R.string.estimate_total_to_pay)} ${"%.2f".format(runningTotal)} ₽",
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = Primary) }
                    Box {
                        IconButton(onClick = { shareMenuExpanded = true }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.share_estimate),
                                tint = Primary
                            )
                        }
                        DropdownMenu(
                            expanded = shareMenuExpanded,
                            onDismissRequest = { shareMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("PDF") },
                                onClick = { shareMenuExpanded = false; onSharePdf() }
                            )
                            DropdownMenuItem(
                                text = { Text("Excel (CSV)") },
                                onClick = { shareMenuExpanded = false; onShareExcel() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditActItemDialog(
    item: IntermediateEstimateActItemEntity,
    onDismiss: () -> Unit,
    onSave: (IntermediateEstimateActItemEntity) -> Unit
) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var quantityText by remember(item.id) { mutableStateOf(item.quantity.toString()) }
    var priceText by remember(item.id) { mutableStateOf(item.price.toString()) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.edit_act_position),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ProfiTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.item_name),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                ProfiTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = stringResource(R.string.quantity),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ProfiTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = stringResource(R.string.price_rub_parens),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Primary) }
                    TextButton(
                        onClick = {
                            val q = quantityText.replace(",", ".").toDoubleOrNull() ?: item.quantity
                            val p = priceText.replace(",", ".").toDoubleOrNull() ?: item.price
                            onSave(
                                item.copy(
                                    name = name.trim().ifEmpty { item.name },
                                    quantity = q.coerceAtLeast(0.0),
                                    price = p.coerceAtLeast(0.0)
                                )
                            )
                        }
                    ) {
                        Text(stringResource(R.string.save), color = Primary)
                    }
                }
            }
        }
    }
}

