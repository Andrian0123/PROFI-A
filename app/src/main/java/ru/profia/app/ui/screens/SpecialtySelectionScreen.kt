package ru.profia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen

/** Список специальностей для выбора при старте (профи). */
val SPECIALTIES = listOf(
    "Строительство",
    "Отделочные работы",
    "Электрика",
    "Сантехника",
    "Кровельные работы",
    "Фасадные работы",
    "Дизайн и проектирование",
    "Прочее"
)

@Composable
fun SpecialtySelectionScreen(
    onSpecialtySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    BaseScreen(
        title = stringResource(R.string.specialty_selection_title),
        onBackClick = onBack
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SPECIALTIES) { specialty ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSpecialtySelected(specialty) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = specialty,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}
