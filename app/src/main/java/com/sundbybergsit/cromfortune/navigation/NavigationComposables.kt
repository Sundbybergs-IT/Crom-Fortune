package com.sundbybergsit.cromfortune.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.activityBoundViewModel
import com.sundbybergsit.cromfortune.contentDescription
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
import com.sundbybergsit.cromfortune.ui.settings.Settings
import com.sundbybergsit.cromfortune.ui.settings.SettingsViewModel
import com.sundbybergsit.cromfortune.ui.settings.SettingsViewModelFactory

@Composable
internal fun AppNavigation(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()
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
                addHomeTopLevel()
                addDashboardTopLevel()
                addNotificationsTopLevel()
                addSettingsTopLevel()
            }
        }
    }
}

private fun NavGraphBuilder.addHomeTopLevel() {
    addHome()
    addDashboard()
    addNotifications()
    addSettings()
}

private fun NavGraphBuilder.addDashboardTopLevel() {
    addHome()
    addDashboard()
    addNotifications()
    addSettings()
}

private fun NavGraphBuilder.addNotificationsTopLevel() {
    addHome()
    addDashboard()
    addNotifications()
    addSettings()
}

private fun NavGraphBuilder.addSettingsTopLevel() {
    addHome()
    addDashboard()
    addNotifications()
    addSettings()
}

private fun NavGraphBuilder.addHome() {
    composable(route = Screen.Home.route) {
        val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = { HomeViewModelFactory() })
        Home(viewModel = homeViewModel)
    }
}

private fun NavGraphBuilder.addDashboard() {
    composable(route = Screen.Dashboard.route) {
        val dashboardViewModel: DashboardViewModel by activityBoundViewModel(factoryProducer = { DashboardViewModelFactory() })
        Dashboard(viewModel = dashboardViewModel)
    }
}

private fun NavGraphBuilder.addNotifications() {
    composable(route = Screen.Notifications.route) {
        val notificationsViewModel: NotificationsViewModel by activityBoundViewModel(factoryProducer = { NotificationsViewModelFactory() })
        Notifications(viewModel = notificationsViewModel)
    }
}

private fun NavGraphBuilder.addSettings() {
    composable(route = Screen.Settings.route) {
        val settingsViewModel: SettingsViewModel by activityBoundViewModel(factoryProducer = { SettingsViewModelFactory() })
        Settings(viewModel = settingsViewModel)
    }
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
        is NavigationItem.ResourceIcon -> painterResource(item.iconResId)
        is NavigationItem.ImageVectorIcon -> rememberVectorPainter(item.iconImageVector)
    }
    val selectedPainter = when (item) {
        is NavigationItem.ResourceIcon -> item.selectedIconResId?.let { painterResource(it) }
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

    class ResourceIcon(
        screen: Screen,
        @StringRes labelResId: Int,
        @StringRes contentDescriptionResId: Int,
        @DrawableRes val iconResId: Int,
        @DrawableRes val selectedIconResId: Int? = null,
    ) : NavigationItem(screen, labelResId, contentDescriptionResId)

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
    NavigationItem.ResourceIcon(
        screen = Screen.Dashboard,
        labelResId = R.string.dashboard_title,
        contentDescriptionResId = R.string.dashboard_title,
        iconResId = R.drawable.ic_dashboard_black_24dp,
        selectedIconResId = R.drawable.ic_dashboard_black_24dp,
    ),
    NavigationItem.ResourceIcon(
        screen = Screen.Notifications,
        labelResId = R.string.notifications_title,
        contentDescriptionResId = R.string.notifications_title,
        iconResId = R.drawable.ic_notifications_black_24dp,
        selectedIconResId = R.drawable.ic_notifications_black_24dp,
    ),
    NavigationItem.ImageVectorIcon(
        screen = Screen.Settings,
        labelResId = R.string.settings_title,
        contentDescriptionResId = R.string.settings_title,
        iconImageVector = Icons.Outlined.Settings,
        selectedImageVector = Icons.Filled.Settings
    )
)
