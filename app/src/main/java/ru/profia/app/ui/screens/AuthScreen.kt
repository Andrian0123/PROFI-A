package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading.collectAsState()
    val backendError by viewModel.errorMessage.collectAsState()

    BaseScreen(
        title = stringResource(R.string.auth_title),
        onBackClick = onBack
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(24.dp)
                .testTag("auth_screen")
        ) {
            Text(
                text = stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            ProfiTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    viewModel.clearError()
                },
                label = stringResource(R.string.auth_phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfiTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                label = stringResource(R.string.auth_password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            backendError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it.ifBlank { stringResource(R.string.auth_error_default) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            RoundedButton(
                onClick = {
                    viewModel.login(
                        login = phoneNumber.trim(),
                        password = password,
                        onSuccess = onAuthComplete
                    )
                },
                text = stringResource(R.string.auth_login),
                enabled = !isLoading && phoneNumber.isNotBlank() && password.isNotBlank()
            )
            Spacer(modifier = Modifier.height(12.dp))
            RoundedButton(
                onClick = {
                    viewModel.register(
                        login = phoneNumber.trim(),
                        password = password,
                        onSuccess = onAuthComplete
                    )
                },
                text = stringResource(R.string.auth_register),
                enabled = !isLoading && phoneNumber.isNotBlank() && password.isNotBlank()
            )
        }
        }
    }
}
