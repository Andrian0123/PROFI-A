package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.R
import ru.profia.app.ui.viewmodel.ProfileViewModel
import androidx.navigation.NavController
import ru.profia.app.ui.navigation.NavRoutes
import ru.profia.app.ui.theme.Divider

@Composable
fun ProfileScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") onSubscribeClick: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    onEditProfile: () -> Unit = {},
    onEditCompany: () -> Unit = {},
    onEditRequisites: () -> Unit = {},
    onChangePassword: (() -> Unit)? = null,
    onTwoFaSettings: (() -> Unit)? = null,
    onDeleteAccount: (() -> Unit)? = null,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.userProfile.collectAsState(initial = null)
    val accountType by viewModel.accountType.collectAsState(initial = "PROFI")
    val normalizedAccountType = if (accountType == "BUSINESS" || accountType == "IP" || accountType == "OOO") "BUSINESS" else "PROFI"
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.profile),
        showBackButton = true,
        onMenuClick = onMenuClick,
        modifier = Modifier.testTag("profile_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Тип личного кабинета: только один вариант — Профи или ИП/ООО
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_cabinet_type),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.profile_single_choice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = normalizedAccountType == "PROFI",
                            onClick = { viewModel.setAccountType("PROFI") },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(R.string.profile_profi),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = normalizedAccountType == "BUSINESS",
                            onClick = { viewModel.setAccountType("BUSINESS") },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(R.string.profile_ip_ooo),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Text(
                        text = if (normalizedAccountType == "PROFI") stringResource(R.string.profile_block_profi) else stringResource(R.string.profile_block_business),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Профиль — показывается всегда (для Профи это единственный блок)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.content_desc_person))
                        Text(stringResource(R.string.profile_section), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                    }
                    Text(
                        "${profile?.lastName ?: ""} ${profile?.firstName ?: ""} ${profile?.middleName ?: ""}".trim().ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(profile?.email ?: "—", style = MaterialTheme.typography.bodyMedium)
                    Text(profile?.phone ?: "—", style = MaterialTheme.typography.bodyMedium)
                    RoundedButton(onClick = onEditProfile, text = stringResource(R.string.profile_edit_btn))
                }
            }
            // О компании — только для ИП/ООО
            if (normalizedAccountType == "BUSINESS") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = stringResource(R.string.content_desc_business))
                            Text(stringResource(R.string.company_section), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                        }
                        Text(profile?.companyName ?: "—", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                        Text(stringResource(R.string.profile_inn_kpp, profile?.inn ?: "—", profile?.kpp ?: "—"), style = MaterialTheme.typography.bodySmall)
                        Text(profile?.legalAddress ?: "—", style = MaterialTheme.typography.bodySmall)
                        RoundedButton(onClick = onEditCompany, text = stringResource(R.string.profile_edit_btn))
                    }
                }
            }
            // Реквизиты доступны только для ИП/ООО
            if (normalizedAccountType == "BUSINESS") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = stringResource(R.string.content_desc_account_balance))
                            Text(stringResource(R.string.requisites_section), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                        }
                        Text(profile?.bankName ?: "—", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                        Text(stringResource(R.string.profile_bank_rs, profile?.accountNumber ?: "—"), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.profile_bank_ks, profile?.correspondentAccount ?: "—"), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.profile_bik, profile?.bic ?: "—"), style = MaterialTheme.typography.bodySmall)
                        RoundedButton(onClick = onEditRequisites, text = stringResource(R.string.profile_edit_btn))
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.ks2_ks3_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.profile_ks2_ks3_desc),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        RoundedButton(
                            onClick = { navController.navigate(NavRoutes.FORM_KS2_KS3) },
                            text = stringResource(R.string.profile_form_ks2_ks3_btn),
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.content_desc_settings))
                        Text(stringResource(R.string.account_section), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onChangePassword?.invoke() ?: onShowSnackbar(context.getString(R.string.profile_nav_to_change_password))
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.change_password), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.list_item_chevron), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(NavRoutes.SETTINGS) }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.list_item_chevron), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTwoFaSettings?.invoke() ?: onShowSnackbar(context.getString(R.string.dev_two_fa))
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.two_fa), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.list_item_chevron), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAccountDialog = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.delete_account), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        Text(stringResource(R.string.list_item_chevron), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(NavRoutes.SPLASH) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.exit), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.list_item_chevron), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (showDeleteAccountDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAccountDialog = false },
                    title = { Text(stringResource(R.string.delete_account_confirm_title)) },
                    text = { Text(stringResource(R.string.delete_account_confirm_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteAccount(
                                onSuccess = {
                                    showDeleteAccountDialog = false
                                    onDeleteAccount?.invoke()
                                },
                                onError = { msg ->
                                    showDeleteAccountDialog = false
                                    onShowSnackbar(msg)
                                }
                            )
                        }) {
                            Text(stringResource(R.string.delete_btn), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAccountDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}
