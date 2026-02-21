package ru.profia.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.profia.app.ui.theme.OnPrimary
import ru.profia.app.ui.theme.Primary

/**
 * Единая кнопка приложения в стиле PROFI-A.
 */
@Composable
fun RoundedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonColor: Color = Primary,
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = OnPrimary,
            disabledContainerColor = buttonColor.copy(alpha = 0.5f)
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = OnPrimary
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = when (contentAlignment) {
                    Alignment.Start -> Arrangement.Start
                    Alignment.End -> Arrangement.End
                    else -> Arrangement.Center
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text)
            }
        }
    }
}

@Composable
fun ProfiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonColor: Color = Primary
) {
    RoundedButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        buttonColor = buttonColor
    )
}
