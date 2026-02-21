package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.viewmodel.TwoFaSettingsViewModel
import androidx.compose.ui.res.stringResource

@Composable
fun TwoFaSettingsScreen(
    navController: NavController,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: TwoFaSettingsViewModel = hiltViewModel()
) {
    val enabled by viewModel.enabled.collectAsState()
    val twoFaTitle = stringResource(R.string.two_fa)
    val twoFaEnableLabel = stringResource(R.string.two_fa_enable_label)
    val twoFaBackendError = stringResource(R.string.two_fa_backend_error)
    val twoFaEnabledMessage = stringResource(R.string.two_fa_enabled_message)
    val twoFaDisabledMessage = stringResource(R.string.two_fa_disabled_message)
    val twoFaSyncHint = stringResource(R.string.two_fa_sync_hint)

    BaseScreen(
        navController = navController,
        title = twoFaTitle,
        showBackButton = true
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = twoFaEnableLabel,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Switch(
                checked = enabled,
                onCheckedChange = { checked ->
                    viewModel.setEnabled(checked) {
                        onShowSnackbar(twoFaBackendError)
                    }
                    onShowSnackbar(
                        if (checked) twoFaEnabledMessage else twoFaDisabledMessage
                    )
                }
            )
            Text(
                text = twoFaSyncHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
