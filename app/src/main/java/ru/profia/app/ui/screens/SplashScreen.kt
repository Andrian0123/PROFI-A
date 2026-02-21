package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.components.SplashLogo
import ru.profia.app.ui.theme.TextSecondary

/**
 * Стартовый экран при запуске приложения:
 * по центру логотип, ниже «Авторизоваться», ниже «Режим демо на 3 дня».
 * Фон в стиле логотипа (градиент).
 */
@Composable
fun SplashScreen(
    onAuthorizeClick: () -> Unit,
    onDemoModeClick: () -> Unit
) {
    BaseScreen(
        title = null,
        showBackButton = false
    ) { _ ->
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SplashLogo(iconSize = 120.dp)
        Spacer(modifier = Modifier.height(48.dp))
        RoundedButton(
            onClick = onAuthorizeClick,
            text = stringResource(R.string.authorize)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onDemoModeClick) {
            Text(
                text = stringResource(R.string.demo_mode_3_days),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
    }
    }
}
