package ru.profia.app.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.viewmodel.DocumentScanViewModel
import java.io.File

@Composable
fun DocumentScanScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null,
    viewModel: DocumentScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loading by viewModel.loading.collectAsState()
    val result by viewModel.result.collectAsState()
    val error by viewModel.error.collectAsState()

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { u ->
            scope.launch {
                val file = withContext(Dispatchers.IO) {
                    copyUriToDocumentFile(context, u)
                }
                if (file != null) {
                    viewModel.scanDocument(file)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.scan_pick_file_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.doc_scan_title),
        showBackButton = true,
        onMenuClick = onMenuClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.doc_scan_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text(
                        stringResource(R.string.doc_scan_analyzing),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.doc_scan_error, err),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            result?.let { r ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.doc_scan_result),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        if (r.widthMm > 0 && r.lengthMm > 0) {
                            Text(
                                stringResource(R.string.doc_scan_size, r.widthMm, r.lengthMm),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (r.hasEngineeringCommunications) {
                            Text(
                                stringResource(R.string.doc_scan_engineering),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (r.content.isNotEmpty()) {
                            Text(
                                stringResource(R.string.doc_scan_content),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            r.content.forEach { label ->
                                Text(
                                    stringResource(R.string.doc_scan_content_item, label.label, label.confidence * 100),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result != null || error != null) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearResult()
                            pickerLauncher.launch("image/*")
                        }
                    ) {
                        Text(stringResource(R.string.doc_scan_pick))
                    }
                }
                if (result == null && error == null && !loading) {
                    Button(
                        onClick = { pickerLauncher.launch("image/*") }
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.content_desc_pick_photo), modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.doc_scan_pick))
                    }
                }
            }
        }
    }
}

private suspend fun copyUriToDocumentFile(context: Context, uri: Uri): File? =
    withContext(Dispatchers.IO) {
        try {
            val dir = File(context.cacheDir, "document_scan").apply { mkdirs() }
            val ext = context.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
            val safeExt = if (ext in listOf("jpeg", "jpg", "png", "webp")) ext else "jpg"
            val file = File(dir, "doc_${System.currentTimeMillis()}.$safeExt")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0) file else null
        } catch (_: Exception) {
            null
        }
    }
