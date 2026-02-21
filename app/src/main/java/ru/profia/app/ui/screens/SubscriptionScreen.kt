package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.data.model.AppSettings
import ru.profia.app.data.model.SubscriptionType
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.R
import ru.profia.app.ui.theme.Pistachio
import ru.profia.app.ui.viewmodel.SettingsViewModel
import ru.profia.app.ui.viewmodel.SubscriptionViewModel
import ru.profia.app.util.LocaleCurrencyHelper
import androidx.navigation.NavController

@Composable
fun SubscriptionScreen(
    navController: NavController,
    subscriptionViewModel: SubscriptionViewModel,
    onMenuClick: (() -> Unit)? = null,
    onShowSnackbar: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isRussian = configuration.locales.size() > 0 && configuration.locales[0].language == "ru"
    val activity = context as? android.app.Activity
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.appSettings.collectAsState(initial = AppSettings())
    val subscription by subscriptionViewModel.subscription.collectAsState()
    val trialDaysRemaining by subscriptionViewModel.trialDaysRemaining.collectAsState()
    var promoCode by remember { mutableStateOf("") }
    var promoMessage by remember { mutableStateOf<String?>(null) }
    var purchaseConfirm by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var paymentMethod by remember { mutableStateOf("qr") } // "qr" | "phone" для русской версии

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.subscription),
        showBackButton = true,
        onMenuClick = onMenuClick,
        modifier = Modifier.testTag("subscription_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (subscription.type == SubscriptionType.TRIAL && trialDaysRemaining != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Пробный период", style = MaterialTheme.typography.titleMedium)
                        Text("Осталось дней: $trialDaysRemaining")
                        // Прогресс считаем исходя из фактической длительности пробного периода
                        val totalDays = if (subscription.startDate > 0 && subscription.endDate > subscription.startDate) {
                            ((subscription.endDate - subscription.startDate) / (24 * 60 * 60 * 1000)).coerceAtLeast(1)
                        } else {
                            14 // значение по умолчанию
                        }
                        val left = trialDaysRemaining ?: 0
                        val used = (totalDays - left).coerceAtLeast(0)
                        val progress = (used.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            if (isRussian) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.payment_method_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            RadioButton(
                                selected = paymentMethod == "qr",
                                onClick = { paymentMethod = "qr" },
                                colors = RadioButtonDefaults.colors(selectedColor = Pistachio)
                            )
                            Text(
                                stringResource(R.string.payment_qr),
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Spacer(modifier = Modifier.width(24.dp))
                            RadioButton(
                                selected = paymentMethod == "phone",
                                onClick = { paymentMethod = "phone" },
                                colors = RadioButtonDefaults.colors(selectedColor = Pistachio)
                            )
                            Text(
                                stringResource(R.string.payment_phone),
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
            SubscriptionPlanCard(
                title = "1 месяц",
                price = LocaleCurrencyHelper.formatPrice(299, settings.currency),
                badge = null,
                onSelect = { purchaseConfirm = "1 месяц" to 1 }
            )
            SubscriptionPlanCard(
                title = "6 месяцев",
                price = LocaleCurrencyHelper.formatPrice(1500, settings.currency),
                badge = "Выгодный пакет",
                onSelect = { purchaseConfirm = "6 месяцев" to 6 }
            )
            SubscriptionPlanCard(
                title = "1 год",
                price = LocaleCurrencyHelper.formatPrice(3200, settings.currency),
                badge = "Максимум выгоды",
                onSelect = { purchaseConfirm = "1 год" to 12 }
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Промокод", style = MaterialTheme.typography.titleMedium)
                    ProfiTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it },
                        label = "Введите промокод",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                    promoMessage?.let { Text(it) }
                    RoundedButton(
                        onClick = {
                            subscriptionViewModel.activatePromoCode(promoCode) { result ->
                                val msg = result.fold(
                                    { "Промокод активирован на $it мес." },
                                    { it.message ?: "Ошибка активации" }
                                )
                                promoMessage = msg
                                onShowSnackbar(msg)
                                if (result.isSuccess) promoCode = ""
                            }
                        },
                        text = "Активировать"
                    )
                }
            }
            purchaseConfirm?.let { (title, months) ->
                AlertDialog(
                    onDismissRequest = { purchaseConfirm = null },
                    title = { Text(stringResource(R.string.confirm_subscription)) },
                    text = { Text("Подписка на $title. Подтвердить оформление?") },
                    confirmButton = {
                        TextButton(onClick = {
                            if (activity != null) {
                                subscriptionViewModel.startPurchase(activity, months) { result ->
                                    result.fold(
                                        onSuccess = {
                                            onShowSnackbar("Подписка оформлена")
                                            purchaseConfirm = null
                                        },
                                        onFailure = { e ->
                                            if (e !is ru.profia.app.billing.BillingManager.CancelledException) {
                                                onShowSnackbar(e.message ?: "Ошибка оплаты")
                                            }
                                            purchaseConfirm = null
                                        }
                                    )
                                }
                            } else {
                                subscriptionViewModel.purchaseSubscription(months)
                                onShowSnackbar("Подписка оформлена")
                                purchaseConfirm = null
                            }
                        }) {
                            Text("Оформить", color = Pistachio)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { purchaseConfirm = null }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    title: String,
    price: String,
    badge: String?,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(price)
            badge?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            RoundedButton(onClick = onSelect, text = "Выбрать")
        }
    }
}
