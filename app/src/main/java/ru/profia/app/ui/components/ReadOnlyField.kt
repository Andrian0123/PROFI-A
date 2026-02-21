package ru.profia.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Поле только для чтения (заблокированное).
 */
@Composable
fun ReadOnlyField(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ProfiTextField(
            value = value,
            onValueChange = { },
            label = label,
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
