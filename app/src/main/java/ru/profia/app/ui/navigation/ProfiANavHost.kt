package ru.profia.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import ru.profia.app.ui.components.ProfiANavigationDrawer
import ru.profia.app.ui.screens.AboutScreen
import ru.profia.app.ui.screens.AddRoomScreen
import ru.profia.app.ui.screens.RoomScanScreen
import ru.profia.app.ui.screens.CalculatorScreen
import ru.profia.app.ui.screens.CreateProjectScreen
import ru.profia.app.ui.screens.HomeScreen
import ru.profia.app.ui.screens.MaterialsScreen
import ru.profia.app.ui.screens.ProjectDetailScreen
import ru.profia.app.ui.screens.EditProfileSectionScreen
import ru.profia.app.ui.screens.FormKs2Ks3Screen
import ru.profia.app.ui.screens.KS2Screen
import ru.profia.app.ui.screens.KS3Screen
import ru.profia.app.ui.screens.ActsScreen
import ru.profia.app.ui.screens.GeneralEstimateScreen
import ru.profia.app.ui.screens.ProfileScreen
import ru.profia.app.ui.screens.RoomTypesScreen
import ru.profia.app.ui.screens.SettingsScreen
import ru.profia.app.ui.screens.SplashScreen
import ru.profia.app.data.model.SuggestAddFloorData
import ru.profia.app.data.model.nextFloorNameIfRelevant
import ru.profia.app.ui.screens.StagesScreen
import ru.profia.app.ui.screens.SpecialtySelectionScreen
import ru.profia.app.ui.screens.BusinessTypeScreen
import ru.profia.app.ui.screens.ChangePasswordScreen
import ru.profia.app.ui.screens.TwoFaSettingsScreen
import ru.profia.app.ui.screens.AuthScreen
import ru.profia.app.ui.screens.SubscriptionScreen
import ru.profia.app.ui.screens.SupportScreen
import ru.profia.app.ui.screens.WorksScreen
import ru.profia.app.ui.screens.WorksSectionScreen
import ru.profia.app.ui.screens.AddWorkTypesScreen
import ru.profia.app.ui.screens.ForemanInviteScreen
import ru.profia.app.ui.screens.LegalDocumentScreen
import ru.profia.app.ui.screens.WorkCategoryScreen
import ru.profia.app.ui.screens.EditProjectScreen
import ru.profia.app.ui.viewmodel.AddRoomViewModel
import ru.profia.app.ui.viewmodel.OnboardingViewModel
import ru.profia.app.ui.viewmodel.ProjectViewModel
import ru.profia.app.ui.viewmodel.SubscriptionViewModel

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.profia.app.ui.navigation.slideInFromRight
import ru.profia.app.ui.navigation.slideOutToLeft
import ru.profia.app.ui.navigation.slideInFromLeft
import ru.profia.app.ui.navigation.slideOutToRight
import ru.profia.app.R

@Composable
fun ProfiANavHost(
    projectViewModel: ProjectViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.split("/")?.firstOrNull() ?: NavRoutes.SPLASH
    val drawerState = remember { androidx.compose.material3.DrawerState(androidx.compose.material3.DrawerValue.Closed) }
    val coroutineScope = rememberCoroutineScope()
    val projects by projectViewModel.projects.collectAsState(initial = emptyList())
    val isReadOnly by subscriptionViewModel.isReadOnly.collectAsState(initial = true)
    val onOpenDrawer: () -> Unit = { coroutineScope.launch { drawerState.open() } }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val showSnackbar: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message, duration = androidx.compose.material3.SnackbarDuration.Short)
        }
    }
    val context = LocalContext.current
    ProfiANavigationDrawer(
        drawerState = drawerState,
        coroutineScope = coroutineScope,
        currentRoute = currentRoute,
        showSubscriptionInMenu = !isReadOnly,
        onNavigate = { route ->
            when (route) {
                "home" -> navController.navigate(NavRoutes.HOME) { launchSingleTop = true; popUpTo(NavRoutes.HOME) { inclusive = true } }
                "profile" -> navController.navigate(NavRoutes.PROFILE) { launchSingleTop = true }
                "calculator" -> navController.navigate(NavRoutes.CALCULATOR) { launchSingleTop = true }
                "materials" -> navController.navigate(NavRoutes.MATERIALS) { launchSingleTop = true }
                "works" -> navController.navigate(NavRoutes.WORKS) { launchSingleTop = true }
                "room_types" -> navController.navigate(NavRoutes.ROOM_TYPES) { launchSingleTop = true }
                "stages" -> navController.navigate(NavRoutes.STAGES) { launchSingleTop = true }
                "subscription" -> navController.navigate(NavRoutes.SUBSCRIPTION) { launchSingleTop = true }
                "settings" -> navController.navigate(NavRoutes.SETTINGS) { launchSingleTop = true }
                "support" -> navController.navigate(NavRoutes.SUPPORT) { launchSingleTop = true }
                "about" -> navController.navigate(NavRoutes.ABOUT) { launchSingleTop = true }
                "form_ks2_ks3" -> navController.navigate(NavRoutes.FORM_KS2_KS3) { launchSingleTop = true }
                "share_app" -> {
                    val shareText = context.getString(R.string.share_app_text)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_app)))
                }
                "foreman" -> navController.navigate(NavRoutes.FOREMAN) { launchSingleTop = true }
                "agreement" -> navController.navigate(NavRoutes.AGREEMENT) { launchSingleTop = true }
                "personal_data" -> navController.navigate(NavRoutes.PERSONAL_DATA) { launchSingleTop = true }
                "privacy_policy" -> navController.navigate(NavRoutes.PRIVACY_POLICY) { launchSingleTop = true }
                "logout" -> coroutineScope.launch {
                    onboardingViewModel.logout {
                        navController.navigate(NavRoutes.SPLASH) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    ) {
        androidx.compose.material3.Scaffold(
            snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavContent(
                    navController = navController,
                    projectViewModel = projectViewModel,
                    subscriptionViewModel = subscriptionViewModel,
                    onboardingViewModel = onboardingViewModel,
                    projects = projects,
                    isReadOnly = isReadOnly,
                    onOpenDrawer = onOpenDrawer,
                    onShowSnackbar = showSnackbar
                )
            }
        }
    }
}

@Composable
private fun NavContent(
    navController: androidx.navigation.NavHostController,
    projectViewModel: ProjectViewModel,
    subscriptionViewModel: ru.profia.app.ui.viewmodel.SubscriptionViewModel,
    onboardingViewModel: OnboardingViewModel,
    projects: List<ru.profia.app.data.model.Project>,
    isReadOnly: Boolean,
    onOpenDrawer: (() -> Unit)? = null,
    onShowSnackbar: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        enterTransition = { slideInFromRight() },
        exitTransition = { slideOutToLeft() },
        popEnterTransition = { slideInFromLeft() },
        popExitTransition = { slideOutToRight() }
    ) {
        composable(NavRoutes.SPLASH) {
            LaunchedEffect(Unit) {
                if (!subscriptionViewModel.isSubscriptionRequired()) {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                } else {
                    val hasSession = onboardingViewModel.hasAuthSession()
                    val onboardingDone = onboardingViewModel.isOnboardingCompleted()
                    if (hasSession || onboardingDone) {
                        subscriptionViewModel.startTrial()
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                }
            }
            SplashScreen(
                onAuthorizeClick = {
                    navController.navigate(NavRoutes.AUTH)
                },
                onDemoModeClick = {
                    navController.navigate(NavRoutes.BUSINESS_TYPE)
                }
            )
        }
        composable(NavRoutes.SPECIALTY) {
            SpecialtySelectionScreen(
                onSpecialtySelected = { specialty ->
                    onboardingViewModel.saveSpecialty(specialty) {
                        navController.navigate(NavRoutes.BUSINESS_TYPE)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.BUSINESS_TYPE) {
            BusinessTypeScreen(
                onBusinessTypeSelected = { accountType ->
                    onboardingViewModel.saveAccountTypeAndComplete(accountType) {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.AUTH) {
            AuthScreen(
                onAuthComplete = {
                    navController.navigate(NavRoutes.BUSINESS_TYPE) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("project/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailScreen(
                navController = navController,
                projectId = projectId,
                isReadOnly = isReadOnly,
                onMenuClick = null,
                onAddRoom = { navController.navigate(NavRoutes.addRoom(it)) },
                onRoomClick = { pid, roomId -> navController.navigate(NavRoutes.addRoom(pid, roomId)) },
                onAddSuggestedFloor = { data ->
                    projectViewModel.setSuggestedRoomForm(projectId, data.nextFloorName, data.formData)
                    navController.navigate(NavRoutes.addRoom(projectId))
                }
            )
        }
        composable(NavRoutes.GENERAL_ESTIMATE) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            GeneralEstimateScreen(
                navController = navController,
                projectId = projectId
            )
        }
        composable(NavRoutes.FINAL_ESTIMATE) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            GeneralEstimateScreen(
                navController = navController,
                projectId = projectId,
                isFinalEstimate = true
            )
        }
        composable(NavRoutes.ACTS) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ActsScreen(
                navController = navController,
                projectId = projectId
            )
        }
        composable(NavRoutes.KS2) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            KS2Screen(navController = navController, projectId = projectId)
        }
        composable(NavRoutes.KS3) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            KS3Screen(navController = navController, projectId = projectId)
        }
        composable(NavRoutes.PROJECTS) {
            LaunchedEffect(Unit) {
                navController.navigate(NavRoutes.HOME) {
                    popUpTo(NavRoutes.PROJECTS) { inclusive = true }
                }
            }
        }
        composable(NavRoutes.HOME) {
            LaunchedEffect(Unit) {
                projectViewModel.ensureDemoProject()
            }
            HomeScreen(
                navController = navController,
                projects = projects,
                isReadOnly = isReadOnly,
                onSubscribeClick = { navController.navigate(NavRoutes.SUBSCRIPTION) },
                onAddProject = { navController.navigate(NavRoutes.CREATE_PROJECT) },
                onProjectClick = { navController.navigate(NavRoutes.projectDetail(it)) },
                onMenuClick = onOpenDrawer
            )
        }
        composable(NavRoutes.CREATE_PROJECT) {
            val lastCreatedId by projectViewModel.lastCreatedProjectId.collectAsState()
            val createProjectError by projectViewModel.createProjectError.collectAsState()
            CreateProjectScreen(
                navController = navController,
                isReadOnly = isReadOnly,
                onSaveAndAddRoom = { projectViewModel.addProject(it, null) },
                onMenuClick = null
            )
            LaunchedEffect(lastCreatedId) {
                lastCreatedId?.let { projectId ->
                    projectViewModel.clearLastCreatedProjectId()
                    onShowSnackbar(navController.context.getString(R.string.project_created))
                    navController.navigate(NavRoutes.addRoom(projectId)) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                }
            }
            LaunchedEffect(createProjectError) {
                if (createProjectError) {
                    onShowSnackbar(navController.context.getString(R.string.project_creation_failed))
                    projectViewModel.clearCreateProjectError()
                }
            }
        }
        composable(NavRoutes.EDIT_PROJECT) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            EditProjectScreen(
                navController = navController,
                projectId = projectId,
                onMenuClick = null
            )
        }
        composable("add_room/{projectId}/{roomId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val roomId = backStackEntry.arguments?.getString("roomId")?.takeIf { it != "new" }
            val addRoomVm: AddRoomViewModel = hiltViewModel()
            AddRoomScreen(
                navController = navController,
                projectId = projectId,
                roomId = roomId,
                addRoomViewModel = addRoomVm,
                onSave = { roomData, openings, workItems ->
                    if (roomId != null) {
                        projectViewModel.updateRoom(roomId, roomData, openings, workItems)
                    } else {
                        projectViewModel.addRoom(projectId, roomData, openings, workItems)
                    }
                    val nextFloorName = nextFloorNameIfRelevant(roomData.name)
                    if (roomId == null && nextFloorName != null) {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "suggestAddFloor",
                            SuggestAddFloorData(nextFloorName, roomData)
                        )
                    }
                    onShowSnackbar(navController.context.getString(R.string.room_saved))
                    navController.popBackStack()
                },
                onMenuClick = null
            )
        }
        composable("room_scan/{projectId}/{roomId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val roomId = backStackEntry.arguments?.getString("roomId") ?: "new"
            RoomScanScreen(
                navController = navController,
                projectId = projectId,
                roomId = roomId
            )
        }
        composable(NavRoutes.CALCULATOR) {
            CalculatorScreen(navController = navController, fromProject = false, onMenuClick = onOpenDrawer)
        }
        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                navController = navController,
                onSubscribeClick = { navController.navigate(NavRoutes.SUBSCRIPTION) },
                onMenuClick = null,
                onEditProfile = { navController.navigate(NavRoutes.editProfileSection("profile")) },
                onEditCompany = { navController.navigate(NavRoutes.editProfileSection("company")) },
                onEditRequisites = { navController.navigate(NavRoutes.editProfileSection("requisites")) },
                onChangePassword = { navController.navigate(NavRoutes.CHANGE_PASSWORD) },
                onTwoFaSettings = { navController.navigate(NavRoutes.TWO_FA) },
                onDeleteAccount = {
                    navController.navigate(NavRoutes.SPLASH) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        composable(NavRoutes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                navController = navController,
                onPasswordChanged = { onShowSnackbar(navController.context.getString(R.string.password_changed_success)) }
            )
        }
        composable(NavRoutes.TWO_FA) {
            TwoFaSettingsScreen(
                navController = navController,
                onShowSnackbar = onShowSnackbar
            )
        }
        composable(NavRoutes.FORM_KS2_KS3) {
            FormKs2Ks3Screen(navController = navController)
        }
        composable("edit_profile/{section}") { backStackEntry ->
            val section = backStackEntry.arguments?.getString("section") ?: "profile"
            EditProfileSectionScreen(
                navController = navController,
                section = section,
                onMenuClick = onOpenDrawer,
                onShowSnackbar = onShowSnackbar
            )
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController, onMenuClick = null)
        }
        composable(NavRoutes.SUBSCRIPTION) {
            SubscriptionScreen(
                navController = navController,
                subscriptionViewModel = subscriptionViewModel,
                onMenuClick = null,
                onShowSnackbar = onShowSnackbar
            )
        }
        composable(NavRoutes.MATERIALS) {
            MaterialsScreen(navController = navController, onMenuClick = null)
        }
        composable(NavRoutes.WORKS) {
            WorksScreen(navController = navController, onMenuClick = null)
        }
        composable(
            NavRoutes.WORK_SECTION,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val sectionId = android.net.Uri.decode(backStackEntry.arguments?.getString("sectionId") ?: "")
            WorksSectionScreen(navController = navController, sectionId = sectionId, onMenuClick = null)
        }
        composable(
            NavRoutes.ADD_WORK_TYPES,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            AddWorkTypesScreen(navController = navController, onMenuClick = null)
        }
        composable(
            NavRoutes.WORK_CATEGORY,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val categoryId = android.net.Uri.decode(backStackEntry.arguments?.getString("categoryId") ?: "")
            WorkCategoryScreen(navController = navController, categoryId = categoryId)
        }
        composable(NavRoutes.ROOM_TYPES) {
            RoomTypesScreen(navController = navController, onMenuClick = null)
        }
        composable(NavRoutes.STAGES) {
            StagesScreen(navController = navController, onMenuClick = null)
        }
        composable(NavRoutes.FOREMAN) {
            ForemanInviteScreen(navController = navController, onMenuClick = null)
        }
        composable(NavRoutes.SUPPORT) {
            SupportScreen(
                navController = navController,
                onMenuClick = null,
                onShowSnackbar = onShowSnackbar
            )
        }
        composable(NavRoutes.ABOUT) {
            AboutScreen(navController = navController, onMenuClick = null)
        }
        composable(NavRoutes.AGREEMENT) {
            LegalDocumentScreen(
                navController = navController,
                titleResId = ru.profia.app.R.string.menu_agreement,
                contentResId = ru.profia.app.R.string.doc_agreement_content,
                onMenuClick = onOpenDrawer
            )
        }
        composable(NavRoutes.PERSONAL_DATA) {
            LegalDocumentScreen(
                navController = navController,
                titleResId = ru.profia.app.R.string.menu_personal_data,
                contentResId = ru.profia.app.R.string.doc_personal_data_content,
                onMenuClick = onOpenDrawer
            )
        }
        composable(NavRoutes.PRIVACY_POLICY) {
            LegalDocumentScreen(
                navController = navController,
                titleResId = ru.profia.app.R.string.menu_privacy_policy,
                contentResId = ru.profia.app.R.string.doc_privacy_policy_content,
                onMenuClick = onOpenDrawer
            )
        }
    }
}
