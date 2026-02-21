package ru.profia.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.data.model.ProjectFormData
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.components.UnsavedChangesDialog
import ru.profia.app.ui.theme.ErrorRed
import androidx.navigation.NavController

/**
 * Экран создания проекта.
 */
@Composable
fun CreateProjectScreen(
    navController: NavController,
    isReadOnly: Boolean = false,
    onSaveAndAddRoom: (ProjectFormData) -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }

    var lastNameError by remember { mutableStateOf(false) }
    var firstNameError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }

    val isValid = lastName.isNotBlank() && firstName.isNotBlank() && address.isNotBlank()
    val hasUnsavedChanges = lastName.isNotBlank() || firstName.isNotBlank() || middleName.isNotBlank() ||
        email.isNotBlank() || phone.isNotBlank() || address.isNotBlank() || city.isNotBlank() ||
        street.isNotBlank() || house.isNotBlank() || apartment.isNotBlank()
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    BackHandler(enabled = hasUnsavedChanges && !isReadOnly) {
        showUnsavedDialog = true
    }

    fun validate(): Boolean {
        lastNameError = lastName.isBlank()
        firstNameError = firstName.isBlank()
        addressError = address.isBlank()
        return !lastNameError && !firstNameError && !addressError
    }

    BaseScreen(
        navController = navController,
        title = "",
        showBackButton = true,
        showSaveButton = !isReadOnly,
        onMenuClick = onMenuClick,
        onBackClick = if (hasUnsavedChanges && !isReadOnly) {
            { showUnsavedDialog = true }
        } else null,
        onSave = {
            if (validate()) {
                onSaveAndAddRoom(
                    ProjectFormData(
                        lastName = lastName.trim(),
                        firstName = firstName.trim(),
                        middleName = middleName.trim().ifBlank { null },
                        email = email.trim().ifBlank { null },
                        phone = phone.trim().ifBlank { null },
                        address = address.trim(),
                        city = city.trim().ifBlank { null },
                        street = street.trim().ifBlank { null },
                        house = house.trim().ifBlank { null },
                        apartment = apartment.trim().ifBlank { null }
                    )
                )
            }
        },
        isSaveEnabled = isValid && !isReadOnly,
        modifier = Modifier.testTag("create_project_screen"),
        actions = {
            IconButton(onClick = { showCalculator = true }) {
                Icon(Icons.Default.Calculate, contentDescription = stringResource(R.string.content_desc_calculator), tint = MaterialTheme.colorScheme.primary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Данные проекта",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    ProfiTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = false
                        },
                        label = "Адрес объекта *",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        isError = addressError,
                        supportingText = if (addressError) { { Text(stringResource(R.string.required_field), color = ErrorRed) } } else null
                    )
                    ProfiTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "Город",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    ProfiTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = "Улица",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfiTextField(
                            value = house,
                            onValueChange = { house = it },
                            label = "Дом",
                            enabled = !isReadOnly,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        )
                        ProfiTextField(
                            value = apartment,
                            onValueChange = { apartment = it },
                            label = "Квартира",
                            enabled = !isReadOnly,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        )
                    }
                }
            }

            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Данные клиента",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    ProfiTextField(
                        value = lastName,
                        onValueChange = {
                            lastName = it
                            lastNameError = false
                        },
                        label = "Фамилия *",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        isError = lastNameError,
                        supportingText = if (lastNameError) { { Text(stringResource(R.string.required_field), color = ErrorRed) } } else null
                    )
                    ProfiTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            firstNameError = false
                        },
                        label = "Имя *",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        isError = firstNameError,
                        supportingText = if (firstNameError) { { Text(stringResource(R.string.required_field), color = ErrorRed) } } else null
                    )
                    ProfiTextField(
                        value = middleName,
                        onValueChange = { middleName = it },
                        label = "Отчество",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    ProfiTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Номер телефона",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    ProfiTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Электронная почта",
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            }
            RoundedButton(
                onClick = {
                    if (validate()) {
                        onSaveAndAddRoom(
                            ProjectFormData(
                                lastName = lastName.trim(),
                                firstName = firstName.trim(),
                                middleName = middleName.trim().ifBlank { null },
                                email = email.trim().ifBlank { null },
                                phone = phone.trim().ifBlank { null },
                                address = address.trim(),
                                city = city.trim().ifBlank { null },
                                street = street.trim().ifBlank { null },
                                house = house.trim().ifBlank { null },
                                apartment = apartment.trim().ifBlank { null }
                            )
                        )
                    }
                },
                text = "Сохранить и добавить комнату",
                enabled = isValid && !isReadOnly
            )
        }
    }
    if (showCalculator) {
        SimpleCalculatorDialog(onDismiss = { showCalculator = false })
    }
    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onSave = {
                if (validate()) {
                    onSaveAndAddRoom(
                        ProjectFormData(
                            lastName = lastName.trim(),
                            firstName = firstName.trim(),
                            middleName = middleName.trim().ifBlank { null },
                            email = email.trim().ifBlank { null },
                            phone = phone.trim().ifBlank { null },
                            address = address.trim(),
                            city = city.trim().ifBlank { null },
                            street = street.trim().ifBlank { null },
                            house = house.trim().ifBlank { null },
                            apartment = apartment.trim().ifBlank { null }
                        )
                    )
                    showUnsavedDialog = false
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
