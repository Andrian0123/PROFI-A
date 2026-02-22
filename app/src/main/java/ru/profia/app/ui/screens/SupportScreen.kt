package ru.profia.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.theme.ErrorRed
import ru.profia.app.util.ValidationUtils
import ru.profia.app.R
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.components.ProfiTextField
import androidx.navigation.NavController
import ru.profia.app.ui.viewmodel.SupportViewModel

@Composable
fun SupportScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: SupportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    var attachedUri by remember { mutableStateOf<Uri?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            attachedUri = it
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    attachedFileName = cursor.getString(nameIndex)
                } else {
                    attachedFileName = it.lastPathSegment?.substringAfterLast('/')
                        ?: context.getString(R.string.support_file_fallback_name)
                }
            } ?: run {
                attachedFileName = it.lastPathSegment?.substringAfterLast('/')
                    ?: context.getString(R.string.support_file_fallback_name)
            }
        }
    }
    var emailError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    val isEmailValid = ValidationUtils.isValidEmail(email)
    val isValid = isEmailValid && description.isNotBlank()

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.support),
        showBackButton = true,
        onMenuClick = onMenuClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .testTag("support_screen")
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.support_contact_info_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.support_contact_email), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                    Text(stringResource(R.string.support_contact_response_time), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.support_form_title), style = MaterialTheme.typography.titleSmall)
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!.ifBlank { context.getString(R.string.support_error_submit) },
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    ProfiTextField(
                        value = email,
                        onValueChange = { email = it; emailError = false; viewModel.clearError() },
                        label = stringResource(R.string.support_email_label),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        isError = emailError,
                        supportingText = if (emailError) {
                            { Text(stringResource(R.string.support_invalid_email), color = ErrorRed) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    ProfiTextField(
                        value = description,
                        onValueChange = { description = it; descriptionError = false; viewModel.clearError() },
                        label = stringResource(R.string.support_description_label),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        minLines = 3,
                        isError = descriptionError,
                        supportingText = if (descriptionError) {
                            { Text(stringResource(R.string.required_field), color = ErrorRed) }
                        } else null
                    )
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = stringResource(R.string.content_desc_attach_file), modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.support_attach_file))
                    }
                    attachedFileName?.let { name ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { attachedFileName = null; attachedUri = null }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.support_delete_attachment))
                            }
                        }
                    }
                    RoundedButton(
                        onClick = {
                            emailError = email.isBlank() || !ValidationUtils.isValidEmail(email)
                            descriptionError = description.isBlank()
                            if (!emailError && !descriptionError) {
                                viewModel.submitAsync(
                                    phone = "",
                                    email = email.trim(),
                                    description = description.trim()
                                ) { result ->
                                    if (result.isSuccess) {
                                        onShowSnackbar(context.getString(R.string.support_sent_server))
                                    } else {
                                        val body = buildString {
                                            append(context.getString(R.string.support_body_email, email))
                                            append("\n\n")
                                            append(context.getString(R.string.support_body_description, description))
                                        }
                                        val intent = if (attachedUri != null) {
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = context.contentResolver.getType(attachedUri!!) ?: "*/*"
                                                putExtra(Intent.EXTRA_EMAIL, arrayOf("profiateam@gmail.com"))
                                                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.support_email_subject))
                                                putExtra(Intent.EXTRA_TEXT, body)
                                                putExtra(Intent.EXTRA_STREAM, attachedUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                        } else {
                                            Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:profiateam@gmail.com").buildUpon()
                                                    .appendQueryParameter("subject", context.getString(R.string.support_email_subject))
                                                    .appendQueryParameter("body", body)
                                                    .build()
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        }
                                        val chooser = Intent.createChooser(intent, context.getString(R.string.support_email_chooser_title))
                                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(chooser)
                                            onShowSnackbar(context.getString(R.string.support_mail_client_opened_fallback))
                                        } else {
                                            onShowSnackbar(context.getString(R.string.support_install_email_client))
                                        }
                                    }
                                }
                            }
                        },
                        text = stringResource(R.string.support_send_request),
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                        enabled = isValid,
                        isLoading = isSubmitting
                    )
                }
            }
        }
    }
}
