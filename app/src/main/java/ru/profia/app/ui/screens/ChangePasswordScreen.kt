package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.viewmodel.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    onPasswordChanged: (String) -> Unit = {},
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    val backendError by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    fun validate(): Boolean {
        if (newPassword.length < 6) {
            errorText = context.getString(R.string.change_password_error_min_length)
            return false
        }
        if (newPassword != repeatPassword) {
            errorText = context.getString(R.string.change_password_error_mismatch)
            return false
        }
        errorText = null
        return true
    }

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.change_password),
        showBackButton = true
    ) { paddingValues ->
        if (showResetPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showResetPasswordDialog = false },
                title = { Text(stringResource(R.string.reset_password)) },
                text = { Text(stringResource(R.string.reset_password_coming_soon)) },
                confirmButton = {
                    TextButton(onClick = { showResetPasswordDialog = false }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.change_password_demo_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            TextButton(
                onClick = { showResetPasswordDialog = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.reset_password),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            ProfiTextField(
                value = oldPassword,
                onValueChange = { value -> oldPassword = value },
                label = stringResource(R.string.change_password_old_label),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            ProfiTextField(
                value = newPassword,
                onValueChange = { value -> newPassword = value },
                label = stringResource(R.string.change_password_new_label),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            ProfiTextField(
                value = repeatPassword,
                onValueChange = { value -> repeatPassword = value },
                label = stringResource(R.string.change_password_repeat_label),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            errorText?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            backendError?.let {
                Text(
                    text = it.ifBlank { stringResource(R.string.change_password_error_default) },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.padding(16.dp))
            RoundedButton(
                onClick = {
                    if (validate()) {
                        viewModel.changePassword(
                            oldPassword = oldPassword,
                            newPassword = newPassword,
                            onSuccess = {
                                onPasswordChanged(newPassword)
                                navController.popBackStack()
                            }
                        )
                    }
                },
                text = stringResource(R.string.save),
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

