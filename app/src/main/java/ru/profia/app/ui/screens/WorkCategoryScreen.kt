package ru.profia.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.profia.app.R
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.theme.Primary
import ru.profia.app.ui.theme.TextSecondary
import ru.profia.app.ui.viewmodel.WorkCategoryViewModel

/** Справочник видов работ по категориям (ключ — categoryId, значение — список string res id). */
private val WORK_REFERENCE: Map<String, List<Int>> = mapOf(
    "ceiling" to listOf(R.string.work_ref_plaster, R.string.work_ref_putty, R.string.work_ref_paint, R.string.work_ref_wallpaper, R.string.work_ref_suspended_ceiling, R.string.work_ref_stretch_ceiling),
    "walls" to listOf(R.string.work_ref_plaster, R.string.work_ref_putty, R.string.work_ref_paint, R.string.work_ref_wallpaper, R.string.work_ref_tiling, R.string.work_ref_panels),
    "floor" to listOf(R.string.work_ref_screed, R.string.work_ref_tiling, R.string.work_ref_laminate, R.string.work_ref_linoleum, R.string.work_ref_parquet, R.string.work_ref_heated_floor),
    "doors" to listOf(R.string.work_ref_interior_door, R.string.work_ref_entrance_door, R.string.work_ref_slopes),
    "windows" to listOf(R.string.work_ref_window_install, R.string.work_ref_slopes, R.string.work_ref_sill, R.string.work_ref_drip),
    "plumbing" to listOf(R.string.work_ref_toilet, R.string.work_ref_sink, R.string.work_ref_bath, R.string.work_ref_pipes, R.string.work_ref_towel_rail),
    "electrical" to listOf(R.string.work_ref_wiring, R.string.work_ref_outlets_switches, R.string.work_ref_panel_board, R.string.work_ref_lighting),
    "ventilation" to listOf(R.string.work_ref_vent_box, R.string.work_ref_hood, R.string.work_ref_vent_valve),
    "other" to listOf(R.string.work_ref_demolition, R.string.work_ref_cleaning, R.string.work_ref_rubbish_removal, R.string.work_ref_other)
)

private fun getCategoryTitleResId(categoryId: String): Int = when (categoryId) {
    "ceiling" -> R.string.work_category_ceiling
    "walls" -> R.string.work_category_walls
    "floor" -> R.string.work_category_floor
    "doors" -> R.string.work_category_doors
    "windows" -> R.string.work_category_windows
    "plumbing" -> R.string.work_category_plumbing
    "electrical" -> R.string.work_category_electrical
    "ventilation" -> R.string.work_category_ventilation
    "other" -> R.string.work_category_other
    else -> R.string.work_category_other
}

/**
 * Экран списка работ в выбранной категории (Потолок, Стены и т.д.).
 * Список из справочника; по кнопке «+» можно добавить работу в «Мои шаблоны» для подстановки в комнате.
 */
@Composable
fun WorkCategoryScreen(
    navController: NavController,
    categoryId: String,
    viewModel: WorkCategoryViewModel = hiltViewModel()
) {
    val workResIds = WORK_REFERENCE[categoryId] ?: emptyList()
    val categoryTitle = stringResource(getCategoryTitleResId(categoryId))
    var workToAddToTemplatesResId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    workToAddToTemplatesResId?.let { resId ->
        val workName = stringResource(resId)
        AlertDialog(
            onDismissRequest = { workToAddToTemplatesResId = null },
            title = { Text(stringResource(R.string.work_category_add_to_templates_title)) },
            text = {
                Text(stringResource(R.string.work_category_add_to_templates_message, workName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addWorkTemplate(workName, categoryId, "кв.м.", 0.0)
                        Toast.makeText(context, context.getString(R.string.work_category_added_to_templates), Toast.LENGTH_SHORT).show()
                        workToAddToTemplatesResId = null
                    }
                ) {
                    Text(stringResource(R.string.add_room_add_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { workToAddToTemplatesResId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    BaseScreen(
        navController = navController,
        title = stringResource(R.string.work_category_screen_title, categoryTitle)
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.work_category_hint),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (workResIds.isEmpty()) {
                Text(
                    text = stringResource(R.string.work_category_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workResIds) { workResId ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(workResId),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { workToAddToTemplatesResId = workResId },
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = stringResource(R.string.content_desc_add_to_templates),
                                        tint = Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
