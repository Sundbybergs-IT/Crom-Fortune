package com.sundbybergsit.cromfortune.navigation

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.sundbybergsit.cromfortune.*
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ShowSnackbarLaunchedEffect
import com.sundbybergsit.cromfortune.theme.MenuColorComposables
import com.sundbybergsit.cromfortune.ui.dashboard.Dashboard
import com.sundbybergsit.cromfortune.ui.dashboard.DashboardViewModel
import com.sundbybergsit.cromfortune.ui.dashboard.DashboardViewModelFactory
import com.sundbybergsit.cromfortune.ui.home.Home
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.home.HomeViewModelFactory
import com.sundbybergsit.cromfortune.ui.notifications.Notifications
import com.sundbybergsit.cromfortune.ui.notifications.NotificationsViewModel
import com.sundbybergsit.cromfortune.ui.notifications.NotificationsViewModelFactory
import com.sundbybergsit.cromfortune.ui.settings.*

@Composable
internal fun AppNavigation(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator
    ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
        val snackbarHostState = remember { SnackbarHostState() }
        val view = LocalView.current
        ShowSnackbarLaunchedEffect(
            dialogHandler = DialogHandler,
            coroutineScope = rememberCoroutineScope(),
            snackbarHostState = snackbarHostState,
            view = view
        )
        Scaffold(
            scaffoldState = scaffoldState,
            bottomBar = {
                BottomNavigation(
                    onNavigationSelected = { selected ->
                        if (getLastRoot(navController = navController) == selected.route && navController.currentDestination?.route != selected.route) {
                            navController.navigate(selected.route) {
                                popUpTo(selected.route)
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(selected.route) {
                                navController.graph.findStartDestination().route?.let {
                                    popUpTo(it) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    navController = navController
                )
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                AnimatedNavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = { defaultCromEnterTransition(initialState, targetState) },
                    exitTransition = { defaultCromExitTransition(initialState, targetState) },
                    popEnterTransition = { defaultCromPopEnterTransition() },
                    popExitTransition = { defaultCromPopExitTransition() },
                ) {
                    addHomeTopLevel(navController = navController)
                    addDashboardTopLevel(navController = navController)
                    addNotificationsTopLevel(navController = navController)
                    addSettingsTopLevel(navController = navController)
                }
            }
        }
    }
}

private fun NavGraphBuilder.addHomeTopLevel(navController: NavHostController) {
    addHome(navController = navController)
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
}

private fun NavGraphBuilder.addDashboardTopLevel(navController: NavHostController) {
    addHome(navController = navController)
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
}

private fun NavGraphBuilder.addNotificationsTopLevel(navController: NavHostController) {
    addHome(navController = navController)
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
}

private fun NavGraphBuilder.addSettingsTopLevel(navController: NavHostController) {
    addHome(navController = navController)
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
    addSettingsBottomSheet(onNavigateTo = { route -> navController.navigate(route) })
}

private fun NavGraphBuilder.addHome(navController: NavHostController) {
    composable(route = Screen.Home.route) {
        val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = { HomeViewModelFactory() })
        Home(viewModel = homeViewModel)
    }
}

private fun NavGraphBuilder.addDashboard(navController: NavHostController) {
    composable(route = Screen.Dashboard.route) {
        val dashboardViewModel: DashboardViewModel by activityBoundViewModel(factoryProducer = { DashboardViewModelFactory() })
        Dashboard(viewModel = dashboardViewModel, onBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.addNotifications(navController: NavHostController) {
    composable(route = Screen.Notifications.route) {
        val context = LocalContext.current
        val notificationsViewModel: NotificationsViewModel by activityBoundViewModel(factoryProducer = {
            NotificationsViewModelFactory(context = context)
        })
        Notifications(viewModel = notificationsViewModel, onBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.addSettings(navController: NavHostController) {
    composable(route = Screen.Settings.route) {
        val settingsViewModel: SettingsViewModel by activityBoundViewModel(factoryProducer = { SettingsViewModelFactory() })
        Settings(viewModel = settingsViewModel, onBack = { navController.popBackStack() },
            onNavigateTo = { route -> navController.navigate(route) })
    }
}

private fun NavGraphBuilder.addSettingsBottomSheet(onNavigateTo: (String) -> Unit) {
    bottomSheet(route = LeafScreen.BottomSheetsSettings.route) { backStackEntry ->
        BottomSheetContent {
            SettingsItems(onNavigateTo = onNavigateTo)
        }
    }
}

@Composable
private fun SettingsItems(onNavigateTo: (String) -> Unit) {
    val context = LocalContext.current
    val showStockRetrievalTimeIntervalsDialog = remember { mutableStateOf(false) }
    if (showStockRetrievalTimeIntervalsDialog.value) {
        // FIXME: Dialog doesn't work, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
        AndroidView(factory = { context ->
            val dialog = TimeIntervalStockRetrievalDialogFragment()
            dialog.onCreateView(
                LayoutInflater.from(context),
                null,
                null
            ) ?: View(context)
        })
    }
    val showSupportedStocksDialog = remember { mutableStateOf(false) }
    if (showSupportedStocksDialog.value) {
        // FIXME: Dialog doesn't work, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
        AndroidView(factory = { context ->
            val dialog = SupportedStockDialogFragment()
            dialog.onCreateView(
                LayoutInflater.from(context),
                null,
                null
            ) ?: View(context)
        })
    }
    BottomSheetMenuItem(
        onClick = { showStockRetrievalTimeIntervalsDialog.value = true },
        text = stringResource(id = R.string.action_configure_stock_retrieval_intervals)
    )
    BottomSheetMenuItem(
        onClick = { showSupportedStocksDialog.value = true },
        text = stringResource(id = R.string.action_stocks_supported)
    )
    BottomSheetMenuItem(
        onClick = {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/Sundbybergs-IT/Crom-Fortune/issues")
            )
            context.startActivity(browserIntent)
        },
        text = stringResource(id = R.string.generic_to_do)
    )
}

private fun AnimatedContentScope<*>.defaultCromEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): EnterTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeIn()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.Start)
}

private fun AnimatedContentScope<*>.defaultCromExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): ExitTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeOut()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.Start)
}

private val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

private fun AnimatedContentScope<*>.defaultCromPopEnterTransition(): EnterTransition {
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.End)
}

private fun AnimatedContentScope<*>.defaultCromPopExitTransition(): ExitTransition {
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.End)
}

@Composable
internal fun BottomNavigation(
    navController: NavHostController,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomNavigation(
        modifier = modifier.contentDescription("Bottom Navigation"),
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = MenuColorComposables.translucentBarAlpha()),
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        elevation = 8.dp,
    ) {
        // This is needed to "listen" to the back stack entry changing, even if android studio think its unused it doesnt work without it.
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        @Suppress("UNUSED_VARIABLE") val currentDestination = navBackStackEntry?.destination

        val selectedRoute = getLastRoot(navController)

        bottomNavigationItems.forEach { item ->
            val isSelected = selectedRoute == item.screen.route
            BottomNavigationItem(
                icon = { NavigationItemIcon(item = item, selected = isSelected) },
                label = {
                    Text(
                        text = stringResource(item.labelResId), color = if (isSelected) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.onSurface
                        },
                        style = MaterialTheme.typography.caption
                    )
                },
                selected = isSelected,
                onClick = { onNavigationSelected(item.screen) },
            )
        }
    }
}

/**
 * Here we evaluate the last "root" we were at, by reversing the backstack and picking the first item that is a root item.
 * And since it starts at null we default to Home.
 */
private fun getLastRoot(navController: NavHostController) =
    navController.backQueue.lastOrNull { backItem ->
        bottomNavigationItems.map { it.screen.route }.any { it == backItem.destination.route }
    }?.destination?.route ?: Screen.Home.route

@Composable
private fun NavigationItemIcon(item: NavigationItem, selected: Boolean) {
    val painter = when (item) {
        is NavigationItem.ImageVectorIcon -> rememberVectorPainter(item.iconImageVector)
    }
    val selectedPainter = when (item) {
        is NavigationItem.ImageVectorIcon -> item.selectedImageVector?.let { rememberVectorPainter(it) }
    }

    if (selectedPainter != null) {
        Crossfade(targetState = selected) {
            Icon(
                tint = if (selected) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface
                },
                painter = if (it) selectedPainter else painter,
                contentDescription = stringResource(item.contentDescriptionResId),
            )
        }
    } else {
        Icon(
            tint = MaterialTheme.colors.primary,
            painter = painter,
            contentDescription = stringResource(item.contentDescriptionResId),
        )
    }
}

private sealed class NavigationItem(
    val screen: Screen,
    @StringRes val labelResId: Int,
    @StringRes val contentDescriptionResId: Int,
) {

    class ImageVectorIcon(
        screen: Screen,
        @StringRes labelResId: Int,
        @StringRes contentDescriptionResId: Int,
        val iconImageVector: ImageVector,
        val selectedImageVector: ImageVector? = null,
    ) : NavigationItem(screen, labelResId, contentDescriptionResId)

}

private val bottomNavigationItems = listOf(
    NavigationItem.ImageVectorIcon(
        screen = Screen.Home,
        labelResId = R.string.home_title,
        contentDescriptionResId = R.string.home_title,
        iconImageVector = Icons.Outlined.Home,
        selectedImageVector = Icons.Filled.Home
    ),
    NavigationItem.ImageVectorIcon(
        screen = Screen.Dashboard,
        labelResId = R.string.dashboard_title,
        contentDescriptionResId = R.string.dashboard_title,
        iconImageVector = Icons.Outlined.Dashboard,
        selectedImageVector = Icons.Filled.Dashboard
    ),
    NavigationItem.ImageVectorIcon(
        screen = Screen.Notifications,
        labelResId = R.string.notifications_title,
        contentDescriptionResId = R.string.notifications_title,
        iconImageVector = Icons.Outlined.Notifications,
        selectedImageVector = Icons.Filled.Notifications
    ),
    NavigationItem.ImageVectorIcon(
        screen = Screen.Settings,
        labelResId = R.string.settings_title,
        contentDescriptionResId = R.string.settings_title,
        iconImageVector = Icons.Outlined.Settings,
        selectedImageVector = Icons.Filled.Settings
    )
)
