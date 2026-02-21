package ru.profia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.profia.app.data.model.ProjectData
import ru.profia.app.data.model.RoomData
import ru.profia.app.data.model.SuggestAddFloorData
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.EmptyState
import ru.profia.app.ui.components.ErrorView
import ru.profia.app.ui.components.LoadingIndicator
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.R
import ru.profia.app.ui.viewmodel.ProjectDetailViewModel
import androidx.navigation.NavController
import ru.profia.app.ui.navigation.NavRoutes

@Composable
fun ProjectDetailScreen(
    navController: NavController,
    projectId: String,
    isReadOnly: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    onAddRoom: (String) -> Unit,
    onRoomClick: (projectId: String, roomId: String) -> Unit = { _, _ -> },
    onAddSuggestedFloor: (SuggestAddFloorData) -> Unit = { }
) {
    val viewModel: ProjectDetailViewModel = hiltViewModel()
    val project by viewModel.project.collectAsState(initial = null)
    val loadError by viewModel.loadError.collectAsState(initial = false)
    val isLoading by viewModel.isLoading.collectAsState(initial = true)
    val totalProjectCost by viewModel.totalProjectCost.collectAsState(initial = 0.0)
    var suggestAddFloor by remember { mutableStateOf<SuggestAddFloorData?>(null) }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }
    LaunchedEffect(navController.currentBackStackEntry) {
        viewModel.loadProject(projectId)
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        val data = navController.currentBackStackEntry?.savedStateHandle?.get<SuggestAddFloorData>("suggestAddFloor")
        if (data != null) {
            navController.currentBackStackEntry?.savedStateHandle?.remove<SuggestAddFloorData>("suggestAddFloor")
            suggestAddFloor = data
        }
    }

    BaseScreen(
        navController = navController,
        title = project?.displayName ?: stringResource(R.string.project),
        showBackButton = true,
        onMenuClick = onMenuClick,
        modifier = Modifier.testTag("project_detail_screen")
    ) { paddingValues ->
        when {
            loadError -> ErrorView(
                message = null,
                onRetry = { viewModel.loadProject(projectId) },
                modifier = Modifier.fillMaxWidth()
            )
            isLoading || project == null -> LoadingIndicator()
            else -> {
                val p = project!!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxSize()
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 0.dp,
                                bottom = 8.dp
                            )
                    ) {
                Text(
                    text = stringResource(R.string.project),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ProjectInfoCard(
                    project = p,
                    onEditClick = if (!isReadOnly) { { navController.navigate(NavRoutes.editProject(projectId)) } } else null
                )

                // Кнопки сметы, актов и добавления комнаты в один ряд
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoundedButton(
                        onClick = { navController.navigate(NavRoutes.generalEstimate(projectId)) },
                        text = stringResource(R.string.preliminary_estimate),
                        modifier = Modifier.weight(1f)
                    )
                    RoundedButton(
                        onClick = { navController.navigate(NavRoutes.acts(projectId)) },
                        text = stringResource(R.string.acts),
                        modifier = Modifier.weight(1f)
                    )
                    if (!isReadOnly) {
                        RoundedButton(
                            onClick = { onAddRoom(projectId) },
                            text = stringResource(R.string.add_room_button_short),
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Start
                        )
                    }
                }

                Text(
                    stringResource(R.string.rooms_count, p.rooms.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
                if (p.rooms.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.no_rooms),
                        subtitle = null,
                        action = if (!isReadOnly) {
                            {
                                RoundedButton(
                                    onClick = { onAddRoom(projectId) },
                                    text = stringResource(R.string.add_room_button)
                                )
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(p.rooms) { room ->
                            RoomCard(
                                room = room,
                                isReadOnly = isReadOnly,
                                onClick = { onRoomClick(projectId, room.id) }
                            )
                        }
                    }
                }
                suggestAddFloor?.let { data ->
                    TextButton(
                        onClick = {
                            onAddSuggestedFloor(data)
                            suggestAddFloor = null
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.add_floor_same_params, data.nextFloorName),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Общая стоимость внизу экрана
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.project_total_cost_short)}: ${"%.2f".format(totalProjectCost)} ₽",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectInfoCard(
    project: ProjectData,
    onEditClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${project.lastName} ${project.firstName} ${project.middleName ?: ""}".trim())
                Text(project.address, style = MaterialTheme.typography.bodyMedium)
                project.phone?.let { Text(it) }
                project.email?.let { Text(it) }
            }
            if (onEditClick != null) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_project)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomCard(
    room: RoomData,
    isReadOnly: Boolean = false,
    onClick: () -> Unit = {}
) {
    val cardModifier = if (isReadOnly) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxWidth().clickable(onClick = onClick)
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(room.name, style = MaterialTheme.typography.titleMedium)
            Text("${stringResource(R.string.floor_area)}: ${"%.2f".format(room.floorArea)} м²")
            Text("${stringResource(R.string.wall_area)}: ${"%.2f".format(room.wallArea)} м²")
        }
    }
}
