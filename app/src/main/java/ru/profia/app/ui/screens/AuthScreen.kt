package ru.profia.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.BuildConfig
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading.collectAsState()
    val backendError by viewModel.errorMessage.collectAsState()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val isRussianLocale = configuration.locales.size() > 0 && configuration.locales[0].language == "ru"
    val authBaseUrl = BuildConfig.AUTH_SERVER_URL

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
            if (isRussianLocale) {
                // Русская версия: только Mail.ru, VK, Яндекс
                Text(
                    text = stringResource(R.string.auth_subtitle_social),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                RoundedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$authBaseUrl/auth/oauth/mail")))
                    },
                    text = stringResource(R.string.auth_login_mail),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                RoundedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$authBaseUrl/auth/oauth/vk")))
                    },
                    text = stringResource(R.string.auth_login_vk),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                RoundedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$authBaseUrl/auth/oauth/yandex")))
                    },
                    text = stringResource(R.string.auth_login_yandex),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
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
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onForgotPassword) {
                    Text(stringResource(R.string.auth_forgot_password))
                }
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
}
