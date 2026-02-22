package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.viewmodel.ResetPasswordViewModel

@Composable
fun RequestResetScreen(
    navController: NavController,
    onSuccessNavigateToReset: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    var loginOrEmail by rememberSaveable { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val requestResult by viewModel.requestResult.collectAsState()

    BaseScreen(
        title = stringResource(R.string.reset_request_title),
        onBackClick = { navController.popBackStack() }
    ) { _ ->
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.reset_request_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            ProfiTextField(
                value = loginOrEmail,
                onValueChange = {
                    loginOrEmail = it
                    viewModel.clearError()
                },
                label = stringResource(R.string.reset_request_login_hint),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            requestResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.reset_request_sent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                result.resetToken?.let { token ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = token,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                RoundedButton(
                    onClick = onSuccessNavigateToReset,
                    text = stringResource(R.string.reset_request_go_to_code)
                )
            }
            if (requestResult == null) {
                Spacer(modifier = Modifier.height(24.dp))
                RoundedButton(
                    onClick = { viewModel.requestReset(loginOrEmail) },
                    text = stringResource(R.string.reset_request_send),
                    enabled = !isLoading && loginOrEmail.isNotBlank()
                )
            }
        }
    }
}
