package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.theme.ErrorRed
import androidx.navigation.NavController

@Composable
fun AboutScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null
) {
    BaseScreen(
        navController = navController,
        title = stringResource(R.string.about),
        showBackButton = true,
        onMenuClick = onMenuClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Название: ПРОФЙ-А", style = MaterialTheme.typography.titleMedium)
                    Text("Версия: 1.0.0", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Text(
                        "Цель приложения: Помощь профессионалам и частным лицам в составлении смет на строительные и ремонтные работы",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text("Дата обновления: 18.02.2026", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Text("Разработчик: ПРОФЙ-А Team", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Text("© 2026 Все права защищены", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "⚠️ Внимание: Если вы не проходили повторную авторизацию более 365 дней, ваш аккаунт автоматически удаляется.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorRed,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Text(
                text = buildAnnotatedString {
                    append("Политика конфиденциальности")
                    addStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline), start = 0, end = length)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            )
        }
    }
}
