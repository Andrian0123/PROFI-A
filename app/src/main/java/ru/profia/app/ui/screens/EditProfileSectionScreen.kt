package ru.profia.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import ru.profia.app.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusDirection
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.data.model.UserProfile
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.components.UnsavedChangesDialog
import ru.profia.app.ui.theme.ErrorRed
import ru.profia.app.util.ValidationUtils
import ru.profia.app.ui.viewmodel.ProfileViewModel
import androidx.navigation.NavController

@Composable
fun EditProfileSectionScreen(
    navController: NavController,
    section: String,
    onMenuClick: (() -> Unit)? = null,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.userProfile.collectAsState(initial = null)
    val baseProfile = profile ?: UserProfile(lastName = "", firstName = "", email = "", phone = "")

    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var inn by remember { mutableStateOf("") }
    var kpp by remember { mutableStateOf("") }
    var legalAddress by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var correspondentAccount by remember { mutableStateOf("") }
    var bic by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var companyNameError by remember { mutableStateOf(false) }
    var innError by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        lastName = baseProfile.lastName
        firstName = baseProfile.firstName
        middleName = baseProfile.middleName ?: ""
        email = baseProfile.email
        phone = baseProfile.phone
        companyName = baseProfile.companyName ?: ""
        inn = baseProfile.inn ?: ""
        kpp = baseProfile.kpp ?: ""
        legalAddress = baseProfile.legalAddress ?: ""
        bankName = baseProfile.bankName ?: ""
        accountNumber = baseProfile.accountNumber ?: ""
        correspondentAccount = baseProfile.correspondentAccount ?: ""
        bic = baseProfile.bic ?: ""
    }

    val hasUnsavedChanges = when (section) {
        "profile" -> lastName != baseProfile.lastName || firstName != baseProfile.firstName ||
            middleName != (baseProfile.middleName ?: "") || email != baseProfile.email || phone != baseProfile.phone
        "company" -> companyName != (baseProfile.companyName ?: "") || inn != (baseProfile.inn ?: "") ||
            kpp != (baseProfile.kpp ?: "") || legalAddress != (baseProfile.legalAddress ?: "")
        "requisites" -> bankName != (baseProfile.bankName ?: "") || accountNumber != (baseProfile.accountNumber ?: "") ||
            correspondentAccount != (baseProfile.correspondentAccount ?: "") || bic != (baseProfile.bic ?: "")
        else -> false
    }

    fun buildMergedProfile(): UserProfile = baseProfile.copy(
        lastName = if (section == "profile") lastName.trim() else baseProfile.lastName,
        firstName = if (section == "profile") firstName.trim() else baseProfile.firstName,
        middleName = if (section == "profile") middleName.trim().ifBlank { null } else baseProfile.middleName,
        email = if (section == "profile") email.trim() else baseProfile.email,
        phone = if (section == "profile") phone.trim() else baseProfile.phone,
        companyName = if (section == "company") companyName.trim().ifBlank { null } else baseProfile.companyName,
        inn = if (section == "company") inn.trim().ifBlank { null } else baseProfile.inn,
        kpp = if (section == "company") kpp.trim().ifBlank { null } else baseProfile.kpp,
        legalAddress = if (section == "company") legalAddress.trim().ifBlank { null } else baseProfile.legalAddress,
        bankName = if (section == "requisites") bankName.trim().ifBlank { null } else baseProfile.bankName,
        accountNumber = if (section == "requisites") accountNumber.trim().ifBlank { null } else baseProfile.accountNumber,
        correspondentAccount = if (section == "requisites") correspondentAccount.trim().ifBlank { null } else baseProfile.correspondentAccount,
        bic = if (section == "requisites") bic.trim().ifBlank { null } else baseProfile.bic
    )

    fun validateProfile(): Boolean {
        return when (section) {
            "profile" -> {
                emailError = email.isNotBlank() && !ValidationUtils.isValidEmail(email)
                phoneError = phone.isBlank()
                !emailError &&
                    !phoneError &&
                    lastName.isNotBlank() &&
                    firstName.isNotBlank() &&
                    phone.isNotBlank()
            }
            "company" -> {
                companyNameError = companyName.isBlank()
                innError = inn.isBlank()
                !companyNameError && !innError
            }
            else -> true
        }
    }

    BackHandler(enabled = hasUnsavedChanges) { showUnsavedDialog = true }

    val focusManager = LocalFocusManager.current

    val title = when (section) {
        "profile" -> stringResource(R.string.edit_profile_section_title_profile)
        "company" -> stringResource(R.string.edit_profile_section_title_company)
        "requisites" -> stringResource(R.string.edit_profile_section_title_requisites)
        else -> stringResource(R.string.edit_profile_section_title_default)
    }

    val snackbarMessageSaved = when (section) {
        "profile" -> stringResource(R.string.edit_profile_snackbar_profile_saved)
        "company" -> stringResource(R.string.edit_profile_snackbar_company_saved)
        "requisites" -> stringResource(R.string.edit_profile_snackbar_requisites_saved)
        else -> ""
    }

    val performSave: () -> Unit = {
        when (section) {
            "profile" -> {
                if (validateProfile()) {
                    viewModel.updateProfile(buildMergedProfile())
                    onShowSnackbar(snackbarMessageSaved)
                    navController.popBackStack()
                }
            }
            "company" -> {
                if (validateProfile()) {
                    viewModel.updateProfile(buildMergedProfile())
                    onShowSnackbar(snackbarMessageSaved)
                    navController.popBackStack()
                }
            }
            "requisites" -> {
                viewModel.updateProfile(buildMergedProfile())
                onShowSnackbar(snackbarMessageSaved)
                navController.popBackStack()
            }
            else -> { }
        }
    }

    val isValid = when (section) {
        "profile" -> lastName.isNotBlank() &&
            firstName.isNotBlank() &&
            phone.isNotBlank() &&
            (email.isBlank() || ValidationUtils.isValidEmail(email))
        "company" -> companyName.isNotBlank() && inn.isNotBlank()
        else -> true
    }

    BaseScreen(
        navController = navController,
        title = title,
        showBackButton = true,
        showSaveButton = true,
        onMenuClick = onMenuClick,
        onBackClick = if (hasUnsavedChanges) { { showUnsavedDialog = true } } else null,
        onSave = performSave,
        isSaveEnabled = isValid
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            when (section) {
                "profile" -> {
                    field(stringResource(R.string.last_name), lastName, { lastName = it }, last = false, onNextAction = { focusManager.moveFocus(FocusDirection.Down) })
                    field(stringResource(R.string.first_name), firstName, { firstName = it }, last = false, onNextAction = { focusManager.moveFocus(FocusDirection.Down) })
                    field(stringResource(R.string.middle_name), middleName, { middleName = it }, last = false, onNextAction = { focusManager.moveFocus(FocusDirection.Down) })
                    field(
                        label = stringResource(R.string.email),
                        value = email,
                        onValueChange = { email = it; emailError = false },
                        last = false,
                        keyboardType = KeyboardType.Email,
                        isError = emailError,
                        errorText = if (emailError) stringResource(R.string.edit_profile_error_email) else null,
                        onNextAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    field(
                        label = stringResource(R.string.edit_profile_phone_required),
                        value = phone,
                        onValueChange = { phone = it; phoneError = false },
                        last = true,
                        keyboardType = KeyboardType.Phone,
                        isError = phoneError,
                        errorText = if (phoneError) stringResource(R.string.edit_profile_error_phone) else null,
                        onDoneAction = { performSave() }
                    )
                }
                "company" -> {
                    field(
                        label = stringResource(R.string.edit_profile_company_name),
                        value = companyName,
                        onValueChange = { companyName = it; companyNameError = false },
                        last = false,
                        isError = companyNameError,
                        errorText = if (companyNameError) stringResource(R.string.edit_profile_error_company_name) else null,
                        onNextAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    field(
                        label = stringResource(R.string.edit_profile_inn),
                        value = inn,
                        onValueChange = { inn = it; innError = false },
                        last = false,
                        keyboardType = KeyboardType.Number,
                        isError = innError,
                        errorText = if (innError) stringResource(R.string.edit_profile_error_inn) else null,
                        onNextAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    field(stringResource(R.string.edit_profile_kpp), kpp, { kpp = it }, last = false, keyboardType = KeyboardType.Number, onNextAction = { focusManager.moveFocus(FocusDirection.Down) })
                    field(stringResource(R.string.edit_profile_legal_address), legalAddress, { legalAddress = it }, last = true, onDoneAction = { performSave() })
                }
                "requisites" -> {
                    field(stringResource(R.string.edit_profile_bank), bankName, { bankName = it }, last = false, onNextAction = { focusManager.moveFocus(FocusDirection.Down) })
                    field(stringResource(R.string.edit_profile_account_number), accountNumber, { accountNumber = it }, last = false, keyboardType = KeyboardType.Number, onNextAction = { focusManager.moveFocus(FocusDirection.Down) })
                    field(stringResource(R.string.edit_profile_correspondent_account), correspondentAccount, { correspondentAccount = it }, last = false, keyboardType = KeyboardType.Number, onNextAction = { focusManager.moveFocus(FocusDirection.Down) })
                    field(stringResource(R.string.edit_profile_bic), bic, { bic = it }, last = true, keyboardType = KeyboardType.Number, onDoneAction = { performSave() })
                }
            }
        }
        if (showUnsavedDialog) {
            UnsavedChangesDialog(
                onSave = {
                    when (section) {
                        "profile", "company", "requisites" -> {
                            if (validateProfile()) {
                                viewModel.updateProfile(buildMergedProfile())
                                onShowSnackbar(snackbarMessageSaved)
                                showUnsavedDialog = false
                                navController.popBackStack()
                            }
                        }
                        else -> { }
                    }
                },
                onDismiss = { showUnsavedDialog = false },
                onExitWithoutSaving = {
                    showUnsavedDialog = false
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    last: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorText: String? = null,
    onNextAction: (KeyboardActionScope.() -> Unit)? = null,
    onDoneAction: (KeyboardActionScope.() -> Unit)? = null
) {
    ProfiTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier.fillMaxWidth().padding(bottom = if (last) 12.dp else 4.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (last) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = onNextAction?.let { block -> { block() } },
            onDone = onDoneAction?.let { block -> { block() } }
        ),
        isError = isError,
        supportingText = if (errorText != null) { { Text(errorText, color = ErrorRed) } } else null
    )
}
