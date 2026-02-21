package ru.profia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.profia.app.R
import ru.profia.app.ui.theme.Pistachio

data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ProfiANavigationDrawer(
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    showSubscriptionInMenu: Boolean = true,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clipToBounds()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    ProfiALogo(
                        logoSize = 48.dp,
                        showText = true,
                        textColor = Pistachio
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    DrawerMenuGroup(
                        items = listOf(
                            DrawerMenuItem(stringResource(R.string.my_profile), Icons.Default.Person, "profile"),
                            DrawerMenuItem(stringResource(R.string.my_projects), Icons.Default.Folder, "home"),
                            DrawerMenuItem(stringResource(R.string.calculator), Icons.Default.Calculate, "calculator"),
                            DrawerMenuItem(stringResource(R.string.ks2_ks3_title), Icons.Default.Article, "form_ks2_ks3"),
                            DrawerMenuItem(stringResource(R.string.add_foreman), Icons.Default.Build, "foreman")
                        ),
                        currentRoute = currentRoute,
                        onItemClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigate(it)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        stringResource(R.string.references),
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = Color.Gray
                    )
                    DrawerMenuGroup(
                        items = listOf(
                            DrawerMenuItem(stringResource(R.string.materials), Icons.Default.ShoppingCart, "materials"),
                            DrawerMenuItem(stringResource(R.string.works), Icons.Default.Build, "works"),
                            DrawerMenuItem(stringResource(R.string.room_types), Icons.Default.Home, "room_types"),
                            DrawerMenuItem(stringResource(R.string.stages), Icons.Default.Timeline, "stages")
                        ),
                        currentRoute = currentRoute,
                        onItemClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigate(it)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        stringResource(R.string.settings_support),
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = Color.Gray
                    )
                    DrawerMenuGroup(
                        items = buildList {
                            if (showSubscriptionInMenu) {
                                add(DrawerMenuItem(stringResource(R.string.subscription), Icons.Default.CardMembership, "subscription"))
                            }
                            add(DrawerMenuItem(stringResource(R.string.settings), Icons.Default.Settings, "settings"))
                            add(DrawerMenuItem(stringResource(R.string.support), Icons.Default.Support, "support"))
                            add(DrawerMenuItem(stringResource(R.string.about), Icons.Default.Info, "about"))
                            add(DrawerMenuItem(stringResource(R.string.share_app), Icons.Default.Share, "share_app"))
                        },
                        currentRoute = currentRoute,
                        onItemClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigate(it)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        stringResource(R.string.menu_documents),
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = Color.Gray
                    )
                    DrawerMenuGroup(
                        items = listOf(
                            DrawerMenuItem(stringResource(R.string.menu_agreement), Icons.Default.Article, "agreement"),
                            DrawerMenuItem(stringResource(R.string.menu_personal_data), Icons.Default.Article, "personal_data"),
                            DrawerMenuItem(stringResource(R.string.menu_privacy_policy), Icons.Default.Article, "privacy_policy")
                        ),
                        currentRoute = currentRoute,
                        onItemClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigate(it)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    DrawerMenuItem(
                        title = stringResource(R.string.exit),
                        icon = Icons.Default.Close,
                        route = "logout"
                    ).let { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch { drawerState.close() }
                                    onNavigate(item.route)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.icon, contentDescription = null, tint = Color.Gray)
                            Text(item.title, modifier = Modifier.padding(start = 16.dp), color = Color.Gray)
                        }
                    }
                }
            }
        },
        content = content
    )
}

@Composable
private fun DrawerMenuGroup(
    items: List<DrawerMenuItem>,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    items.forEach { item ->
        val selected = currentRoute == item.route
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(item.route) }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = item.title,
                color = if (selected) Pistachio else Color.Black
            )
        }
    }
}
