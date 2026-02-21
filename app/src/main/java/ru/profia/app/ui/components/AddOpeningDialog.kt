package ru.profia.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.data.model.OpeningFormData
import ru.profia.app.data.model.OpeningType
import ru.profia.app.ui.theme.Primary

@Composable
fun AddOpeningDialog(
    onDismiss: () -> Unit,
    onAdd: (OpeningFormData) -> Unit
) {
    var type by remember { mutableStateOf(OpeningType.DOOR) }
    var widthStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("1") }

    val width = widthStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val height = heightStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val count = countStr.toIntOrNull() ?: 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_opening_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_opening_type), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                Row {
                    androidx.compose.material3.RadioButton(
                        selected = type == OpeningType.DOOR,
                        onClick = { type = OpeningType.DOOR }
                    )
                    Text(stringResource(R.string.add_opening_door), modifier = Modifier.padding(top = 12.dp).clickable { type = OpeningType.DOOR })
                    androidx.compose.material3.RadioButton(
                        selected = type == OpeningType.WINDOW,
                        onClick = { type = OpeningType.WINDOW }
                    )
                    Text(stringResource(R.string.add_opening_window), modifier = Modifier.padding(top = 12.dp).clickable { type = OpeningType.WINDOW })
                }
                ProfiTextField(
                    value = widthStr,
                    onValueChange = { widthStr = it },
                    label = stringResource(R.string.add_opening_width_m),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ProfiTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = stringResource(R.string.add_opening_height_m),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ProfiTextField(
                    value = countStr,
                    onValueChange = { countStr = it },
                    label = stringResource(R.string.add_opening_count),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (width > 0 && height > 0 && count > 0) {
                    onAdd(OpeningFormData(type = type, width = width, height = height, count = count))
                    onDismiss()
                }
            }) {
                Text(stringResource(R.string.add_room_add_btn), color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
