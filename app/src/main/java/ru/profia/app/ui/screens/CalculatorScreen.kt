package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import androidx.navigation.NavController

/**
 * Экран калькулятора из меню: при открытии сразу показывается тот же
 * всплывающий калькулятор (SimpleCalculatorDialog) с историей, как в комнате и видах работ.
 * При закрытии диалога выполняется возврат назад.
 */
@Composable
fun CalculatorScreen(
    navController: NavController,
    fromProject: Boolean = false,
    onApplyToProject: ((Double) -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    BaseScreen(
        navController = navController,
        title = stringResource(R.string.calculator),
        showBackButton = true,
        onMenuClick = onMenuClick,
        onBackClick = { navController.popBackStack() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.calculator_open_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    SimpleCalculatorDialog(
        onDismiss = { navController.popBackStack() },
        onApply = if (fromProject) onApplyToProject else null
    )
}
