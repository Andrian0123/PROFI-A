package ru.profia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import ru.profia.app.data.model.AppSettings
import ru.profia.app.ui.util.AppLocaleHelper
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.SelectionDialog
import ru.profia.app.R
import ru.profia.app.ui.viewmodel.SettingsViewModel
import ru.profia.app.ui.navigation.NavRoutes
import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController

private val CITIES = listOf(
    "Краснодар", "Москва", "Санкт-Петербург", "Ростов-на-Дону",
    "Новосибирск", "Екатеринбург", "Казань", "Нижний Новгород", "Челябинск", "Самара"
)
private val CURRENCIES = listOf("RUR", "USD", "EUR")
// Список языков сокращённо: RU, EN, RO, UZ, DE, TJ, KZ
private val LANGUAGES = listOf("RU", "EN", "RO", "UZ", "DE", "TJ", "KZ")
private val UNIT_SYSTEMS = listOf("Метры", "Сантиметры")

@Composable
fun SettingsScreen(
    navController: NavController,
    onMenuClick: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.appSettings.collectAsState(initial = AppSettings())
    var showDialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.settings),
        showBackButton = true,
        onMenuClick = onMenuClick,
        modifier = Modifier.testTag("settings_screen")
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDialog = SettingsDialog.CITY }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.city), style = MaterialTheme.typography.bodyLarge)
                        Text("${settings.city} >", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDialog = SettingsDialog.CURRENCY }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.currency), style = MaterialTheme.typography.bodyLarge)
                        Text("${settings.currency} >", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDialog = SettingsDialog.LANGUAGE }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyLarge)
                        Text("${settings.language} >", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        stringResource(R.string.settings_language_restart_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDialog = SettingsDialog.UNIT_SYSTEM }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.unit_system), style = MaterialTheme.typography.bodyLarge)
                        Text("${settings.unitSystem} >", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        stringResource(R.string.menu_documents),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(NavRoutes.AGREEMENT) { launchSingleTop = true } }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.menu_agreement), style = MaterialTheme.typography.bodyLarge)
                        Text(">", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(NavRoutes.PERSONAL_DATA) { launchSingleTop = true } }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.menu_personal_data), style = MaterialTheme.typography.bodyLarge)
                        Text(">", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(NavRoutes.PRIVACY_POLICY) { launchSingleTop = true } }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.menu_privacy_policy), style = MaterialTheme.typography.bodyLarge)
                        Text(">", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            when (showDialog) {
                SettingsDialog.CITY -> SelectionDialog(
                    title = stringResource(R.string.select_city),
                    options = CITIES,
                    selectedOption = settings.city,
                    onSelect = {
                        viewModel.updateSettings(settings.copy(city = it))
                        showDialog = null
                    },
                    onDismiss = { showDialog = null }
                )
                SettingsDialog.CURRENCY -> SelectionDialog(
                    title = stringResource(R.string.select_currency),
                    options = CURRENCIES,
                    selectedOption = settings.currency,
                    onSelect = {
                        viewModel.updateSettings(settings.copy(currency = it))
                        showDialog = null
                    },
                    onDismiss = { showDialog = null }
                )
                SettingsDialog.LANGUAGE -> SelectionDialog(
                    title = stringResource(R.string.select_language),
                    options = LANGUAGES,
                    selectedOption = settings.language,
                    onSelect = { newLang ->
                        showDialog = null
                        scope.launch {
                            viewModel.updateSettingsAndWait(settings.copy(language = newLang))
                            AppLocaleHelper.applyLanguage(newLang)
                            (context as? ComponentActivity)?.recreate()
                        }
                    },
                    onDismiss = { showDialog = null }
                )
                SettingsDialog.UNIT_SYSTEM -> SelectionDialog(
                    title = stringResource(R.string.unit_system),
                    options = UNIT_SYSTEMS,
                    selectedOption = settings.unitSystem,
                    onSelect = {
                        viewModel.updateSettings(settings.copy(unitSystem = it))
                        showDialog = null
                    },
                    onDismiss = { showDialog = null }
                )
                null -> {}
            }
        }
    }
}

private enum class SettingsDialog { CITY, CURRENCY, LANGUAGE, UNIT_SYSTEM }
