package ru.profia.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import ru.profia.app.ui.theme.Divider
import ru.profia.app.ui.theme.Primary
import ru.profia.app.ui.theme.Secondary

@Composable
fun ProfiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: (@Composable (() -> Unit))? = null,
    placeholder: (@Composable (() -> Unit))? = null,
    supportingText: (@Composable (() -> Unit))? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        supportingText = supportingText,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Secondary.copy(alpha = 0.1f),
            unfocusedContainerColor = Secondary.copy(alpha = 0.1f),
            focusedIndicatorColor = Primary,
            unfocusedIndicatorColor = Divider,
            focusedLabelColor = Primary,
            cursorColor = Primary
        )
    )
}
