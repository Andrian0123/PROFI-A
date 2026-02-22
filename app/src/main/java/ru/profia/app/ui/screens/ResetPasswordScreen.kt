package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.viewmodel.ResetPasswordViewModel

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    onSuccessBackToAuth: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    var token by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val success by viewModel.resetSuccess.collectAsState()
    val displayError = validationError ?: error
    val mismatchMessage = stringResource(R.string.reset_password_mismatch)

    LaunchedEffect(success) {
        if (success) {
            onSuccessBackToAuth()
        }
    }

    BaseScreen(
        title = stringResource(R.string.reset_password_title),
        onBackClick = { navController.popBackStack() }
    ) { _ ->
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.reset_password_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            ProfiTextField(
                value = token,
                onValueChange = { token = it; viewModel.clearError() },
                label = stringResource(R.string.reset_password_token_hint),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfiTextField(
                value = newPassword,
                onValueChange = { newPassword = it; viewModel.clearError() },
                label = stringResource(R.string.reset_password_new_hint),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfiTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; viewModel.clearError() },
                label = stringResource(R.string.reset_password_confirm_hint),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            displayError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            RoundedButton(
                onClick = {
                    validationError = null
                    viewModel.clearError()
                    if (newPassword != confirmPassword) {
                        validationError = mismatchMessage
                        return@RoundedButton
                    }
                    viewModel.resetPassword(token, newPassword)
                },
                text = stringResource(R.string.reset_password_submit),
                enabled = !isLoading && token.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            )
        }
    }
}
