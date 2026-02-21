package ru.profia.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.viewmodel.ProjectViewModel

/**
 * Экран приглашения прораба / рабочего к управлению проектом.
 * По кнопке открывается почтовый клиент с темой и текстом приглашения.
 */
@Composable
fun ForemanInviteScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null,
    projectViewModel: ProjectViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val projects by projectViewModel.projects.collectAsState()
    var selectedProjectId by remember { mutableStateOf<String?>(projects.firstOrNull()?.id) }
    var email by remember { mutableStateOf("") }
    var canManageRooms by remember { mutableStateOf(true) }
    var canChangePrices by remember { mutableStateOf(false) }
    var canChangeEstimates by remember { mutableStateOf(false) }
    var canEditStages by remember { mutableStateOf(false) }

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.add_foreman),
        showBackButton = true,
        onMenuClick = onMenuClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.foreman_object_project), style = MaterialTheme.typography.titleSmall)
                    // Простое выпадающее поле заменим текстом первого проекта, пока без сложного выбора
                    Text(
                        text = projects.firstOrNull { it.id == selectedProjectId }?.name
                            ?: stringResource(R.string.foreman_project_selected_later),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.foreman_access_rights), style = MaterialTheme.typography.titleSmall)
                    PermissionRow(
                        checked = canManageRooms,
                        onCheckedChange = { canManageRooms = it },
                        text = stringResource(R.string.foreman_can_manage_rooms)
                    )
                    PermissionRow(
                        checked = canChangePrices,
                        onCheckedChange = { canChangePrices = it },
                        text = stringResource(R.string.foreman_can_change_prices)
                    )
                    PermissionRow(
                        checked = canChangeEstimates,
                        onCheckedChange = { canChangeEstimates = it },
                        text = stringResource(R.string.foreman_can_change_estimates)
                    )
                    PermissionRow(
                        checked = canEditStages,
                        onCheckedChange = { canEditStages = it },
                        text = stringResource(R.string.foreman_can_edit_stages)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.foreman_contacts), style = MaterialTheme.typography.titleSmall)
                    ProfiTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email прораба",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.foreman_after_auth_hint),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            RoundedButton(
                onClick = {
                    val projectName = projects.firstOrNull { it.id == selectedProjectId }?.name ?: context.getString(R.string.foreman_project)
                    val subject = Uri.encode("Приглашение в проект: $projectName")
                    val body = Uri.encode("""
                        Здравствуйте!
                        Вас приглашают присоединиться к проекту «$projectName» в приложении ПРОФЙ-А.
                        Права доступа: помещения — ${if (canManageRooms) "да" else "нет"}, цены — ${if (canChangePrices) "да" else "нет"}, сметы — ${if (canChangeEstimates) "да" else "нет"}, этапы — ${if (canEditStages) "да" else "нет"}.
                        Скачайте приложение ПРОФЙ-А и войдите под своей учётной записью, чтобы принять приглашение.
                    """.trimIndent())
                    val mailto = "mailto:${Uri.encode(email.trim())}?subject=$subject&body=$body"
                    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(mailto) }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        navController.popBackStack()
                    }
                },
                text = stringResource(R.string.foreman_generate_and_send),
                enabled = selectedProjectId != null && email.isNotBlank()
            )
        }
    }
}

@Composable
private fun PermissionRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

