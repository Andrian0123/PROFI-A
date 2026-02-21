package ru.profia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.profia.app.R
import ru.profia.app.data.model.Project
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.EmptyState
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.components.SubscriptionBanner
import ru.profia.app.ui.util.LocalAdaptivePadding
import androidx.navigation.NavController

/**
 * Главный экран с последними проектами и нижней навигацией.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    projects: List<Project>,
    isReadOnly: Boolean = false,
    onSubscribeClick: () -> Unit = {},
    onAddProject: () -> Unit,
    onProjectClick: (String) -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    BaseScreen(
        navController = navController,
        title = null,
        showBackButton = false,
        onMenuClick = onMenuClick ?: { },
        modifier = Modifier.testTag("home_screen")
    ) { paddingValues ->
        val adaptive = LocalAdaptivePadding.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = adaptive.horizontal, vertical = adaptive.vertical)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        adaptive.contentMaxWidth?.let { maxW ->
                            Modifier.widthIn(max = maxW).align(Alignment.Center)
                        } ?: Modifier.fillMaxWidth()
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
            if (isReadOnly) {
                SubscriptionBanner(
                    onSubscribeClick = onSubscribeClick,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Text(
                text = stringResource(R.string.recent_projects),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (projects.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.no_projects),
                    subtitle = stringResource(R.string.empty_state_subtitle),
                    action = if (!isReadOnly) {
                        {
                            RoundedButton(
                                onClick = onAddProject,
                                text = stringResource(R.string.add_project)
                            )
                        }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = stringResource(R.string.project),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects.take(3)) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { onProjectClick(project.id) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            RoundedButton(
                onClick = onAddProject,
                text = stringResource(R.string.add_project),
                enabled = !isReadOnly
            )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = project.address,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.date,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
