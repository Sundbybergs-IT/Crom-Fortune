package com.sundbybergsit.cromfortune.navigation

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.sundbybergsit.cromfortune.*
import com.sundbybergsit.cromfortune.R
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { hostData ->
                    Snackbar {
                        val lineColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
                        val backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor),
                        ) {
                            val (textRef, leftLineRef) = createRefs()
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .background(lineColor)
                                    .constrainAs(leftLineRef) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        height = Dimension.fillToConstraints
                                    }
                                    .contentDescription("Left Line"),
                            )
                            Text(
                                hostData.visuals.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .constrainAs(textRef) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(leftLineRef.end, 18.dp)
                                    }
                                    .padding(end = 18.dp)
                                    .contentDescription("Text"),
                            )
                        }
                    }
                }
            },
            bottomBar = {
                BottomNavigation(
                    onNavigationSelected = { selected ->
                        if (navController.getLastRootRoute(bottomNavigationItems) == selected.route && navController.currentDestination?.route != selected.route) {
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
                NavHost(
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
    addHomeBottomSheet()
    addHomeStockBottomSheet()
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
    addNotificationsBottomSheet()
}

private fun NavGraphBuilder.addSettingsTopLevel(navController: NavHostController) {
    addHome(navController = navController)
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
    addSettingsBottomSheet()
}

private fun NavGraphBuilder.addHome(navController: NavHostController) {
    composable(route = Screen.Home.route) {
        val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = { HomeViewModelFactory() })
        Home(viewModel = homeViewModel, onNavigateTo = { route -> navController.navigate(route) })
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
        Notifications(
            viewModel = notificationsViewModel,
            onBack = { navController.popBackStack() },
            onNavigateTo = { route -> navController.navigate(route) })
    }
}

private fun NavGraphBuilder.addSettings(navController: NavHostController) {
    composable(route = Screen.Settings.route) {
        val settingsViewModel: SettingsViewModel by activityBoundViewModel(factoryProducer = { SettingsViewModelFactory() })
        Settings(viewModel = settingsViewModel, onBack = { navController.popBackStack() },
            onNavigateTo = { route -> navController.navigate(route) })
    }
}

private fun NavGraphBuilder.addHomeBottomSheet() {
    bottomSheet(route = LeafScreen.BottomSheetsHome.route) {
        val context = LocalContext.current
        val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
            HomeViewModelFactory()
        })
        BottomSheetContent {
            HomeItems(onBuy = { homeViewModel.showRegisterBuyStocksDialog.value = true },
                onSell = { homeViewModel.showRegisterSellStocksDialog.value = true },
                onSplit = { homeViewModel.showRegisterSplitStocksDialog.value = true },
                onRefresh = {
                    homeViewModel.refreshData(context)
                    Toast.makeText(context, R.string.home_information_data_refreshed, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

private fun NavGraphBuilder.addHomeStockBottomSheet() {
    bottomSheet(
        route = LeafScreen.BottomSheetsHomeStock.route,
        arguments = listOf(
            navArgument("stock_symbol") {
                type = NavType.StringType
                nullable = true
            }),
    ) { backStackEntry ->
        val arguments = checkNotNull(backStackEntry.arguments)
        val stockSymbol = checkNotNull(arguments.getString("stock_symbol"))
        val context = LocalContext.current
        val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
            HomeViewModelFactory()
        })
        val onDelete = { homeViewModel.onClickRemove(context = context, stockName = stockSymbol) }
        BottomSheetContent {
            BottomSheetMenuItem(
                onClick = onDelete,
                text = stringResource(id = R.string.action_delete)
            )
        }
    }
}

private fun NavGraphBuilder.addNotificationsBottomSheet() {
    bottomSheet(route = LeafScreen.BottomSheetsNotifications.route) {
        val context = LocalContext.current
        val notificationsViewModel: NotificationsViewModel by activityBoundViewModel(factoryProducer = {
            NotificationsViewModelFactory(context = context)
        })
        BottomSheetContent {
            NotificationsItems(onClear = { notificationsViewModel.clearNotifications() })
        }
    }
}

private fun NavGraphBuilder.addSettingsBottomSheet() {
    bottomSheet(route = LeafScreen.BottomSheetsSettings.route) {
        val context = LocalContext.current
        val settingsViewModel: SettingsViewModel by activityBoundViewModel(factoryProducer = {
            SettingsViewModelFactory()
        })
        if (settingsViewModel.showStockRetrievalTimeIntervalsDialog.value) {
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
        if (settingsViewModel.showSupportedStocksDialog.value) {
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
        BottomSheetContent {
            SettingsItems(
                onShowSupportedStocks = { settingsViewModel._showSupportedStocksDialog.value = true },
                onShowStockRetrievalTimeIntervals = {
                    settingsViewModel._showStockRetrievalTimeIntervalsDialog.value = true
                },
                onShowTodo = {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Sundbybergs-IT/Crom-Fortune/issues")
                    )
                    context.startActivity(browserIntent)
                }
            )
        }
    }
}

@Composable
private fun HomeItems(onBuy: () -> Unit, onSell: () -> Unit, onSplit: () -> Unit, onRefresh: () -> Unit) {
    BottomSheetMenuItem(
        onClick = onBuy,
        text = stringResource(id = R.string.action_stock_buy)
    )
    BottomSheetMenuItem(
        onClick = onSell,
        text = stringResource(id = R.string.action_stock_sell)
    )
    BottomSheetMenuItem(
        onClick = onSplit,
        text = stringResource(id = R.string.action_stock_add_split)
    )
}

@Composable
private fun NotificationsItems(onClear: () -> Unit) {
    BottomSheetMenuItem(
        onClick = onClear,
        text = stringResource(id = R.string.action_clear)
    )
}

@Composable
private fun SettingsItems(
    onShowSupportedStocks: () -> Unit,
    onShowStockRetrievalTimeIntervals: () -> Unit,
    onShowTodo: () -> Unit,
) {
    BottomSheetMenuItem(
        onClick = onShowStockRetrievalTimeIntervals,
        text = stringResource(id = R.string.action_configure_stock_retrieval_intervals)
    )
    BottomSheetMenuItem(
        onClick = onShowSupportedStocks,
        text = stringResource(id = R.string.action_stocks_supported)
    )
    BottomSheetMenuItem(
        onClick = onShowTodo,
        text = stringResource(id = R.string.generic_to_do)
    )
}

private fun AnimatedContentTransitionScope<*>.defaultCromEnterTransition(
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
    return fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start)
}

private fun AnimatedContentTransitionScope<*>.defaultCromExitTransition(
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
    return fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start)
}

private val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

private fun AnimatedContentTransitionScope<*>.defaultCromPopEnterTransition(): EnterTransition {
    return fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End)
}

private fun AnimatedContentTransitionScope<*>.defaultCromPopExitTransition(): ExitTransition {
    return fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End)
}

@Composable
internal fun BottomNavigation(
    navController: NavHostController,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material.BottomNavigation(
        modifier = modifier.contentDescription("Bottom Navigation"),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
        elevation = 8.dp,
    ) {
        // This is needed to "listen" to the back stack entry changing, even if android studio think its unused it doesnt work without it.
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        @Suppress("UNUSED_VARIABLE") val currentDestination = navBackStackEntry?.destination

        val selectedRoute = navController.getLastRootRoute(bottomNavigationItems)

        bottomNavigationItems.forEach { item ->
            val isSelected = selectedRoute == item.screen.route
            BottomNavigationItem(
                icon = { NavigationItemIcon(item = item, selected = isSelected) },
                label = {
                    Text(
                        text = stringResource(item.labelResId), color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = { onNavigationSelected(item.screen) },
            )
        }
    }
}

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
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                painter = if (it) selectedPainter else painter,
                contentDescription = stringResource(item.contentDescriptionResId),
            )
        }
    } else {
        Icon(
            tint = MaterialTheme.colorScheme.primary,
            painter = painter,
            contentDescription = stringResource(item.contentDescriptionResId),
        )
    }
}

internal sealed class NavigationItem(
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
