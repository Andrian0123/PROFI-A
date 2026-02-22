package ru.profia.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.data.local.entity.IntermediateEstimateActEntity
import ru.profia.app.data.local.entity.IntermediateEstimateActItemEntity
import ru.profia.app.ui.theme.Primary
import ru.profia.app.ui.theme.TextPrimary
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.export.Ks2Ks3Export
import ru.profia.app.ui.viewmodel.FormKs2Ks3ViewModel

@Composable
fun FormKs2Ks3Screen(
    navController: NavController,
    viewModel: FormKs2Ks3ViewModel = hiltViewModel()
) {
    val projects by viewModel.projects.collectAsState()
    val acts by viewModel.acts.collectAsState()
    val actItems by viewModel.actItems.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val selectedActId by viewModel.selectedActId.collectAsState()
    val profile by viewModel.userProfile.collectAsState(initial = null)
    val accountType by viewModel.userAccountType.collectAsState()
    var showFormatDialog by remember { mutableStateOf(false) }
    var actToView by remember { mutableStateOf<IntermediateEstimateActEntity?>(null) }
    var docTypeKs2 by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedAct = remember(selectedActId, acts) { acts.find { it.id == selectedActId } }
    val totalSum = actItems.sumOf { it.total }
    val canGenerate = profile != null && selectedAct != null && actItems.isNotEmpty()

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

    if (showFormatDialog && canGenerate) {
        Ks2Ks3FormatDialog(
            onDismiss = { showFormatDialog = false },
            onPdf = {
                showFormatDialog = false
                val p = profile!!
                val act = selectedAct!!
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        if (docTypeKs2) Ks2Ks3Export.exportKs2Pdf(context, p, act, actItems, totalSum, accountType)
                        else Ks2Ks3Export.exportKs3Pdf(context, p, act, actItems, totalSum, accountType)
                    }
                    if (file != null) shareFile(file, "application/pdf")
                    else Toast.makeText(context, context.getString(R.string.export_pdf_failed), Toast.LENGTH_LONG).show()
                }
            },
            onExcel = {
                showFormatDialog = false
                val p = profile!!
                val act = selectedAct!!
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        if (docTypeKs2) Ks2Ks3Export.exportKs2Csv(context, p, act, actItems, totalSum, accountType)
                        else Ks2Ks3Export.exportKs3Csv(context, p, act, actItems, totalSum, accountType)
                    }
                    if (file != null) shareFile(file, "text/csv")
                    else Toast.makeText(context, context.getString(R.string.export_csv_failed), Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.ks2_ks3_title),
        showBackButton = true
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
                stringResource(R.string.form_ks2_ks3_intro),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(stringResource(R.string.doc_type_label), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                RadioButton(selected = docTypeKs2, onClick = { docTypeKs2 = true })
                Text(stringResource(R.string.ks2_option), modifier = Modifier.clickable { docTypeKs2 = true })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                RadioButton(selected = !docTypeKs2, onClick = { docTypeKs2 = false })
                Text(stringResource(R.string.ks3_option), modifier = Modifier.clickable { docTypeKs2 = false })
            }
            Text(stringResource(R.string.project), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
            projects.forEach { project ->
                val selected = project.id == selectedProjectId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable { viewModel.setSelectedProject(project.id) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(project.name, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (selectedProjectId != null) {
                Text(stringResource(R.string.act_intermediate_section), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                if (acts.isEmpty()) {
                    Text(stringResource(R.string.no_acts_in_project), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                } else {
                    acts.forEach { act ->
                        val selected = act.id == selectedActId
                        val dateTimeStr = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(act.createdAt))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { viewModel.setSelectedAct(act.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.intermediate_estimate), style = MaterialTheme.typography.bodyMedium)
                                    Text(dateTimeStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.setSelectedAct(act.id)
                                        actToView = act
                                    },
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Visibility,
                                        contentDescription = stringResource(R.string.content_desc_view_estimate),
                                        tint = Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (profile == null) {
                Text(
                    stringResource(R.string.form_ks2_ks3_fill_requisites),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            RoundedButton(
                text = stringResource(R.string.generate_and_share),
                onClick = {
                    when {
                        profile == null -> Toast.makeText(context, context.getString(R.string.form_ks2_ks3_fill_requisites_toast), Toast.LENGTH_LONG).show()
                        selectedAct == null || actItems.isEmpty() -> Toast.makeText(context, context.getString(R.string.form_ks2_ks3_select_act_toast), Toast.LENGTH_LONG).show()
                        else -> showFormatDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                enabled = canGenerate
            )
        }
    }

    actToView?.let { act ->
        if (act.id == selectedActId) {
            ViewActDialog(
                act = act,
                items = actItems,
                onDismiss = { actToView = null }
            )
        }
    }
}

@Composable
private fun ViewActDialog(
    act: IntermediateEstimateActEntity,
    items: List<IntermediateEstimateActItemEntity>,
    onDismiss: () -> Unit
) {
    val dateStr = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(act.createdAt))
    val totalSum = items.sumOf { it.total }
    val scrollState = rememberScrollState()
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
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${item.category}: ${item.name}", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${"%.2f".format(item.quantity)} ${item.unitAbbr} × ${"%.2f".format(item.price)} ₽ = ${"%.2f".format(item.total)} ₽",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    "${stringResource(R.string.total_label)}: ${"%.2f".format(totalSum)} ₽",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text(stringResource(R.string.close), color = Primary)
                }
            }
        }
    }
}

@Composable
private fun Ks2Ks3FormatDialog(
    onDismiss: () -> Unit,
    onPdf: () -> Unit,
    onExcel: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.choose_format_and_share),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onPdf, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.export_format_pdf)) }
                    TextButton(onClick = onExcel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.export_format_csv)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    }
}
