package ru.profia.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.ui.theme.ErrorRed
import ru.profia.app.ui.theme.Pistachio

/**
 * Диалог при выходе с несохранёнными изменениями.
 */
@Composable
fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onExitWithoutSaving: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.unsaved_changes_title)) },
        text = {
            Column {
                Text(stringResource(R.string.unsaved_changes_message))
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onExitWithoutSaving) {
                    Text(stringResource(R.string.exit_without_saving), color = ErrorRed)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.save), color = Pistachio)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
