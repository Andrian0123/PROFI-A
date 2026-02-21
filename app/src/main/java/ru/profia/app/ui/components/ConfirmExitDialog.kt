package ru.profia.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import ru.profia.app.R
import ru.profia.app.ui.theme.ErrorRed
import ru.profia.app.ui.theme.Pistachio

/**
 * Диалог подтверждения выхода (для калькулятора).
 */
@Composable
fun ConfirmExitDialog(
    message: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_title)) },
        text = { Text(message ?: stringResource(R.string.calculator_exit_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.exit), color = ErrorRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.stay), color = Pistachio)
            }
        }
    )
}
