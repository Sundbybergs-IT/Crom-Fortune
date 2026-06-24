package com.sundbybergsit.cromfortune.main.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRateApi
import com.sundbybergsit.cromfortune.main.BottomSheetContent
import com.sundbybergsit.cromfortune.main.BottomSheetMenuItem
import com.sundbybergsit.cromfortune.main.DialogHandler
import com.sundbybergsit.cromfortune.main.PortfolioRepository
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.ShowSnackbarLaunchedEffect
import com.sundbybergsit.cromfortune.main.UpdateTimePickerLaunchedEffect
import com.sundbybergsit.cromfortune.main.activityBoundViewModel
import com.sundbybergsit.cromfortune.main.contentDescription
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.main.stocks.StockSplitRepository
import com.sundbybergsit.cromfortune.main.ui.DayPicker
import com.sundbybergsit.cromfortune.main.ui.PortfolioAddAlertDialog
import com.sundbybergsit.cromfortune.main.ui.RegisterBuyStockAlertDialog
import com.sundbybergsit.cromfortune.main.ui.RegisterSellStockAlertDialog
import com.sundbybergsit.cromfortune.main.ui.RegisterSplitStockAlertDialog
import com.sundbybergsit.cromfortune.main.ui.dashboard.Dashboard
import com.sundbybergsit.cromfortune.main.ui.dashboard.DashboardViewModel
import com.sundbybergsit.cromfortune.main.ui.dashboard.DashboardViewModelFactory
import com.sundbybergsit.cromfortune.main.ui.home.Home
import com.sundbybergsit.cromfortune.main.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.main.ui.home.HomeViewModelFactory
import com.sundbybergsit.cromfortune.main.ui.home.view.OpinionatedStockOrderWrapper
import com.sundbybergsit.cromfortune.main.ui.notifications.Notifications
import com.sundbybergsit.cromfortune.main.ui.notifications.NotificationsViewModel
import com.sundbybergsit.cromfortune.main.ui.notifications.NotificationsViewModelFactory
import com.sundbybergsit.cromfortune.main.ui.settings.Settings
import com.sundbybergsit.cromfortune.main.ui.settings.SettingsViewModel
import com.sundbybergsit.cromfortune.main.ui.settings.SettingsViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.Currency
import java.util.Date
import java.util.Locale
import androidx.navigation3.runtime.NavKey as androidxNavKey

private const val DATE_FORMAT = "MM/dd/yyyy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppNavigation(portfolioRepository: PortfolioRepository) {
    val context = LocalContext.current
    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }

    val homeBackStack = rememberNavBackStack(Home)
    val dashboardBackStack = rememberNavBackStack(Dashboard)
    val notificationsBackStack = rememberNavBackStack(Notifications)
    val settingsBackStack = rememberNavBackStack(Settings)

    val backStacks = remember {
        mapOf(
            Home to homeBackStack as MutableList<androidxNavKey>,
            Dashboard to dashboardBackStack as MutableList<androidxNavKey>,
            Notifications to notificationsBackStack as MutableList<androidxNavKey>,
            Settings to settingsBackStack as MutableList<androidxNavKey>
        )
    }

    var currentTabRoute by rememberSaveable { mutableStateOf(Home.toRoute()) }
    val currentTab = currentTabRoute.toNavKey()
    val currentBackStack = backStacks[currentTab]!!

    val onNavigate: (NavKey) -> Unit = { key ->
        currentBackStack.add(key)
    }

    val onBack: () -> Unit = {
        if (currentBackStack.size > 1) {
            currentBackStack.removeAt(currentBackStack.size - 1)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current
    ShowSnackbarLaunchedEffect(
        dialogHandler = DialogHandler,
        snackbarHostState = snackbarHostState,
        view = view
    )

    AddDialogs(dialogHandler = DialogHandler, portfolioRepository = portfolioRepository)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
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
                            text = hostData.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .constrainAs(textRef) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(leftLineRef.end, 18.dp)
                                }
                                .padding(end = 18.dp)
                                .contentDescription("Snackbar Message"),
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigation(
                currentTab = currentTab,
                onNavigationSelected = { selectedTab ->
                    if (currentTab == selectedTab) {
                        if (currentBackStack.size > 1) {
                            currentBackStack.retainAll(listOf(currentBackStack.first()))
                        }
                    } else {
                        currentTabRoute = selectedTab.toRoute()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->
        val entryProvider = entryProvider<androidxNavKey> {
            entry<Home> {
                val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                    HomeViewModelFactory(portfolioRepository = portfolioRepository)
                })
                Home(
                    viewModel = homeViewModel,
                    onNavigateTo = { route -> onNavigate(route.toNavKey()) },
                    appUpdateManager = appUpdateManager
                )
            }

            entry<Dashboard> {
                val dashboardViewModel: DashboardViewModel by activityBoundViewModel(factoryProducer = { DashboardViewModelFactory() })
                Dashboard(viewModel = dashboardViewModel, onBack = onBack)
            }

            entry<Notifications> {
                val localContext = LocalContext.current
                val notificationsViewModel: NotificationsViewModel by activityBoundViewModel(factoryProducer = {
                    NotificationsViewModelFactory(context = localContext)
                })
                Notifications(
                    viewModel = notificationsViewModel,
                    onBack = onBack,
                    onNavigateTo = { route -> onNavigate(route.toNavKey()) })
            }

            entry<Settings> {
                val settingsViewModel: SettingsViewModel by activityBoundViewModel(factoryProducer = { SettingsViewModelFactory() })
                Settings(viewModel = settingsViewModel, onBack = onBack,
                    onNavigateTo = { route -> onNavigate(route.toNavKey()) })
            }

            entry<BottomSheetsHome> {
                val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                    HomeViewModelFactory(portfolioRepository = portfolioRepository)
                })
                BottomSheetContent {
                    HomeItems(
                        portfolioRepository = portfolioRepository,
                        homeViewModel = homeViewModel,
                        onBuy = { DialogHandler.showBuyStockDialog() },
                        onSell = { DialogHandler.showSellStockDialog() },
                        onSplit = { DialogHandler.showSplitStockDialog() },
                        onAddPortfolio = { DialogHandler.showAddPortfolioDialog() },
                    )
                }
            }

            entry<BottomSheetsHomeAllStocks> { key ->
                val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                    HomeViewModelFactory(portfolioRepository = portfolioRepository)
                })
                val onSortNameAscending = { homeViewModel.sortNameAscending(key.profile) }
                val onSortNameDescending = { homeViewModel.sortNameDescending(key.profile) }
                val onSortProfitAscending = { homeViewModel.sortProfitAscending(key.profile) }
                val onSortProfitDescending = { homeViewModel.sortProfitDescending(key.profile) }
                BottomSheetContent {
                    BottomSheetMenuItem(
                        onClick = onSortNameAscending,
                        text = stringResource(id = R.string.home_sort_alphabetical_down)
                    )
                    BottomSheetMenuItem(
                        onClick = onSortNameDescending,
                        text = stringResource(id = R.string.home_sort_alphabetical_up)
                    )
                    BottomSheetMenuItem(
                        onClick = onSortProfitAscending,
                        text = stringResource(id = R.string.home_sort_profit_down)
                    )
                    BottomSheetMenuItem(
                        onClick = onSortProfitDescending,
                        text = stringResource(id = R.string.home_sort_profit_up)
                    )
                }
            }

            entry<BottomSheetsHomeStock> { key ->
                val localContext = LocalContext.current
                val onDelete = {
                    DialogHandler.showDeleteDialog(
                        context = localContext,
                        portfolioName = key.portfolioName,
                        stockName = key.stockSymbol
                    )
                }
                BottomSheetContent {
                    BottomSheetMenuItem(
                        onClick = onDelete,
                        text = stringResource(id = R.string.action_delete)
                    )
                }
            }

            entry<BottomSheetsNotifications> {
                val localContext = LocalContext.current
                val notificationsViewModel: NotificationsViewModel by activityBoundViewModel(factoryProducer = {
                    NotificationsViewModelFactory(context = localContext)
                })
                BottomSheetContent {
                    NotificationsItems(onClear = { notificationsViewModel.clearNotifications() })
                }
            }

            entry<BottomSheetsSettings> {
                val localContext = LocalContext.current
                BottomSheetContent {
                    SettingsItems(
                        onShowSupportedStocks = { DialogHandler.showSupportedStocksDialog() },
                        onShowStockRetrievalTimeIntervals = {
                            DialogHandler.showStockRetrievalTimeIntervalsDialog(StockRetrievalSettings(localContext))
                        },
                        onShowTodo = {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/Sundbybergs-IT/Crom-Fortune/issues".toUri()
                            )
                            localContext.startActivity(browserIntent)
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NavDisplay(
                backStack = currentBackStack,
                onBack = onBack,
                entryProvider = entryProvider
            )

            // Handle Bottom Sheets as Overlays
            val lastKey = currentBackStack.last()
            if (lastKey::class.simpleName?.startsWith("BottomSheets") == true) {
                ModalBottomSheet(onDismissRequest = onBack) {
                    entryProvider(lastKey).Content()
                }
            }
        }
    }
}

private fun String.toNavKey(): NavKey {
    return when {
        this == "home" -> Home
        this == "dashboard" -> Dashboard
        this == "notifications" -> Notifications
        this == "settings" -> Settings
        this == "bottom-sheet/home" -> BottomSheetsHome
        this.startsWith("bottom-sheet/home/stocks") -> {
            val profile = this.substringAfter("profile=", "default")
            BottomSheetsHomeAllStocks(profile)
        }

        this.startsWith("bottom-sheet/home/stock") -> {
            val portfolioName = this.substringAfter("portfolio_name=").substringBefore("&")
            val stockSymbol = this.substringAfter("stock_symbol=")
            BottomSheetsHomeStock(portfolioName, stockSymbol)
        }

        this == "bottom-sheet/notifications" -> BottomSheetsNotifications
        this == "bottom-sheet/settings" -> BottomSheetsSettings
        else -> throw IllegalArgumentException("Unknown route: $this")
    }
}

private fun NavKey.toRoute(): String {
    return when (this) {
        Home -> "home"
        Dashboard -> "dashboard"
        Notifications -> "notifications"
        Settings -> "settings"
        BottomSheetsHome -> "bottom-sheet/home"
        is BottomSheetsHomeAllStocks -> "bottom-sheet/home/stocks?profile=$profile"
        is BottomSheetsHomeStock -> "bottom-sheet/home/stock?portfolio_name=$portfolioName&stock_symbol=$stockSymbol"
        BottomSheetsNotifications -> "bottom-sheet/notifications"
        BottomSheetsSettings -> "bottom-sheet/settings"
    }
}

@Composable
fun AddDialogs(
    dialogHandler: DialogHandler,
    portfolioRepository: PortfolioRepository,
) {
    val portfolioNameState = portfolioRepository.selectedPortfolioNameStateFlow.collectAsState()
    when (val dialogViewState = dialogHandler.dialogViewState.collectAsState().value) {
        is DialogHandler.DialogViewState.ShowDeleteDialog -> {
            DeleteDialog(dialogViewState, onDismiss = { dialogHandler.dismissDialog() })
        }

        is DialogHandler.DialogViewState.ShowStockRetrievalTimeIntervalsDialog -> {
            StockRetrievalTimeIntervalsDialog(dialogViewState, onDismiss = { dialogHandler.dismissDialog() })
        }

        is DialogHandler.DialogViewState.ShowSupportedStocksDialog -> {
            SupportedStocksDialog(dialogViewState, onDismiss = { dialogHandler.dismissDialog() })
        }

        DialogHandler.DialogViewState.Dismissed -> {
            // Do nothing
        }

        is DialogHandler.DialogViewState.ShowStockEvents -> {
            StockEventsDialog(dialogViewState, onDismiss = { dialogHandler.dismissDialog() })
        }

        is DialogHandler.DialogViewState.ShowBuyStockDialog -> {
            val localContext = LocalContext.current
            val savedText = stringResource(id = R.string.generic_saved)
            val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                HomeViewModelFactory(portfolioRepository = portfolioRepository)
            })
            RegisterBuyStockAlertDialog(portfolioNameState = portfolioNameState, onDismiss = {
                dialogHandler.dismissDialog()
            }, stockSymbolParam = dialogViewState.stockSymbol) { stockOrder ->
                homeViewModel.save(
                    context = localContext,
                    portfolioName = portfolioNameState.value,
                    stockOrder = stockOrder
                )
                Toast.makeText(localContext, savedText, Toast.LENGTH_SHORT).show()
            }
        }

        is DialogHandler.DialogViewState.ShowSellStockDialog -> {
            val localContext = LocalContext.current
            val savedText = stringResource(id = R.string.generic_saved)
            val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                HomeViewModelFactory(portfolioRepository = portfolioRepository)
            })
            RegisterSellStockAlertDialog(
                portfolioNameState = portfolioNameState,
                onDismiss = {
                    dialogHandler.dismissDialog()
                },
                stockSymbolParam = dialogViewState.stockSymbol,
                onSave = { stockOrder ->
                    homeViewModel.save(
                        context = localContext,
                        portfolioName = portfolioNameState.value,
                        stockOrder = stockOrder
                    )
                    Toast.makeText(localContext, savedText, Toast.LENGTH_SHORT)
                        .show()
                },
                homeViewModel = homeViewModel,
                portfolioRepository = portfolioRepository,
            )
        }

        is DialogHandler.DialogViewState.ShowRegisterSplitStockDialog -> {
            val localContext = LocalContext.current
            val savedText = stringResource(id = R.string.generic_saved)
            val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                HomeViewModelFactory(portfolioRepository = portfolioRepository)
            })
            RegisterSplitStockAlertDialog(
                portfolioNameState = portfolioNameState,
                onDismiss = { DialogHandler.dismissDialog() }, stockSymbolParam = dialogViewState.stockSymbol
            ) { stockSplit ->
                homeViewModel.save(context = localContext, stockSplit = stockSplit)
                Toast.makeText(localContext, savedText, Toast.LENGTH_SHORT).show()
            }
        }

        is DialogHandler.DialogViewState.ShowAddPortfolio -> {
            val localContext = LocalContext.current
            val savedText = stringResource(id = R.string.generic_saved)
            val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                HomeViewModelFactory(portfolioRepository = portfolioRepository)
            })
            PortfolioAddAlertDialog(onDismiss = { DialogHandler.dismissDialog() }) { portfolioName ->
                homeViewModel.savePortfolio(context = localContext, portfolioName = portfolioName)
                Toast.makeText(localContext, savedText, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun DeleteDialog(
    state: DialogHandler.DialogViewState.ShowDeleteDialog,
    onDismiss: () -> Unit
) {
    val context: Context = LocalContext.current
    AlertDialog(
        modifier = Modifier,
        title = {
            Text(
                text = stringResource(id = R.string.generic_dialog_title_are_you_sure),
                style = MaterialTheme.typography.titleSmall
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.home_delete_all_stock_orders, state.stockName),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.onClickRemove(context = context, stockSymbol = state.stockName)
                onDismiss()
            }) {
                Text(stringResource(id = R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_close))
            }
        }
    )
}

@Composable
private fun StockRetrievalTimeIntervalsDialog(
    state: DialogHandler.DialogViewState.ShowStockRetrievalTimeIntervalsDialog,
    onDismiss: () -> Unit
) {
    val selectedDaysMutableState: MutableState<Set<DayOfWeek>> = remember { mutableStateOf(setOf()) }
    val fromTimePickerState: MutableState<TimePickerState?> = remember { mutableStateOf(null) }
    val toTimePickerState: MutableState<TimePickerState?> = remember { mutableStateOf(null) }
    val settingsViewState by state.stockRetrievalSettings.timeInterval
    UpdateTimePickerLaunchedEffect(
        settingsViewState, state, selectedDaysMutableState,
        fromTimePickerState, toTimePickerState
    )
    val savedText = stringResource(id = R.string.generic_saved)
    if (fromTimePickerState.value != null && toTimePickerState.value != null) {
        AlertDialog(
            modifier = Modifier,
            title = {
                Text(
                    text = stringResource(id = R.string.settings_dialog_time_intervals_title),
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                Column {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.generic_time_start_label),
                        style = MaterialTheme.typography.titleSmall
                    )
                    fromTimePickerState.value?.let { nullSafeTimePickerState ->
                        TimeInput(state = nullSafeTimePickerState)
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.generic_time_end_label),
                        style = MaterialTheme.typography.titleSmall
                    )
                    toTimePickerState.value?.let { nullSafeTimePickerState ->
                        TimeInput(state = nullSafeTimePickerState)
                    }
                    DayPicker(selectedDays = selectedDaysMutableState, onDaySelected = { day ->
                        if (selectedDaysMutableState.value.contains(day)) {
                            selectedDaysMutableState.value -= day
                        } else {
                            selectedDaysMutableState.value += day
                        }
                    })
                }
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val nullSafeFromTimePickerState = checkNotNull(fromTimePickerState.value)
                    val nullSafeToTimePickerState = checkNotNull(toTimePickerState.value)
                    state.stockRetrievalSettings.set(nullSafeFromTimePickerState.hour,
                        nullSafeFromTimePickerState.minute,
                        nullSafeToTimePickerState.hour,
                        nullSafeToTimePickerState.minute,
                        selectedDaysMutableState.value.map { materialWeekday ->
                            DayOfWeek.valueOf(
                                materialWeekday.name
                            )
                        })
                    onDismiss()
                    DialogHandler.showSnack(savedText)
                }) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SupportedStocksDialog(
    state: DialogHandler.DialogViewState.ShowSupportedStocksDialog,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier,
        title = {
            Text(
                text = stringResource(id = R.string.action_stocks_supported),
                style = MaterialTheme.typography.titleSmall
            )
        },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = state.text,
                style = MaterialTheme.typography.titleSmall
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
private fun StockEventsDialog(
    state: DialogHandler.DialogViewState.ShowStockEvents,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val stockEvents = state.stockEvents
    val opinionatedEvents: List<OpinionatedStockOrderWrapper> = getOpinionatedStockOrders(
        stockEvents,
        CromFortuneV1RecommendationAlgorithm(context)
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
        ) {
            ConstraintLayout(modifier = Modifier.padding(8.dp)) {
                val (tableRef, buttonRef) = createRefs()
                LazyColumn(modifier = Modifier
                    .constrainAs(tableRef) {
                        bottom.linkTo(buttonRef.top)
                    }
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth()) {
                    stickyHeader {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface)
                        ) {
                            Row {
                                Text(
                                    text = stringResource(id = R.string.generic_title_stock_orders),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 32.dp)
                            ) {
                                Text(
                                    text = state.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(id = R.string.generic_date),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(id = R.string.generic_title_quantity),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(id = R.string.generic_price_per_stock),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(id = R.string.generic_title_total_cost),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    items(stockEvents.size) { index ->
                        stockEvents[index].stockOrder?.let { nullSafeStockOrder ->
                            val opinionatedStockOrder =
                                opinionatedEvents.single { opinionatedStockOrderWrapper -> opinionatedStockOrderWrapper.stockOrder == nullSafeStockOrder }
                            StockOrderRow(
                                stockOrder = nullSafeStockOrder,
                                opinionatedStockOrder = opinionatedStockOrder,
                                readOnly = state.readOnly
                            )
                        }
                    }
                }
                TextButton(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .constrainAs(buttonRef) {
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        },
                    onClick = onDismiss
                ) {
                    Text(stringResource(id = R.string.action_close).uppercase())
                }
            }
        }
    }
}

// FIXME: Move calculation to view model
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
private fun getOpinionatedStockOrders(
    stockEvents: List<StockEvent>,
    recommendationAlgorithm: RecommendationAlgorithm
): List<OpinionatedStockOrderWrapper> {
    val stockOrderEvents = stockEvents.filter { it.stockOrder != null }.toList()
    val currencyRateInSek =
        checkNotNull(CurrencyRateRepository.currencyRates).value
            .find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrderEvents.first().stockOrder!!.currency }!!.rateInSek
    val opinionatedStockOrderWrappers: MutableList<OpinionatedStockOrderWrapper> =
        mutableListOf()
    for (stockOrderEvent in stockOrderEvents) {
        with(checkNotNull(stockOrderEvent.stockOrder)) {
            opinionatedStockOrderWrappers.add(
                OpinionatedStockOrderWrapper(
                    this,
                    recommendationAlgorithm.getRecommendation(
                        StockPrice(
                            name,
                            Currency.getInstance(currency),
                            pricePerStock
                        ),
                        currencyRateInSek, commissionFee,
                        stockOrderEvents.subList(0, stockOrderEvents.indexOf(stockOrderEvent))
                            .toSet(),
                        stockOrderEvent.dateInMillis
                    )
                )
            )
        }
    }
    return opinionatedStockOrderWrappers.toList()
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
internal fun StockOrderRow(
    modifier: Modifier = Modifier,
    stockOrder: StockOrder,
    opinionatedStockOrder: OpinionatedStockOrderWrapper,
    showDeleteDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    currencyRateApi: CurrencyRateApi = CurrencyRateRepository,
    readOnly: Boolean
) {
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val nf: NumberFormat = NumberFormat.getCurrencyInstance()
    if (stockOrder.pricePerStock < 1) {
        nf.maximumFractionDigits = 3
    } else {
        nf.maximumFractionDigits = 2
    }
    nf.currency = Currency.getInstance(opinionatedStockOrder.stockOrder.currency)
    val backgroundColor = colorResource(
        id =
        if (stockOrder.orderAction == "Buy") {
            android.R.color.holo_green_light
        } else {
            android.R.color.holo_red_light
        }
    )
    val context = LocalContext.current
    val stockEventDate = sdf.format(Date(stockOrder.dateInMillis))
    if (showDeleteDialog.value) {
        AlertDialog(onDismissRequest = { showDeleteDialog.value = false },
            title = {
                Text(
                    text = stringResource(id = R.string.generic_dialog_title_are_you_sure),
                    style = MaterialTheme.typography.titleMedium
                )
            }, text = {
                Text(
                    text = stringResource(id = R.string.home_delete_stock_order, stockEventDate),
                    style = MaterialTheme.typography.titleMedium
                )
            }, confirmButton = {
                TextButton(onClick = {
                    val stockSplitApi: StockSplitApi = StockSplitRepository(
                        context = context,
                        porfolioName = PortfolioRepository.selectedPortfolioNameStateFlow.value
                    )
                    val listOfSplits = stockSplitApi.list(stockOrder.name)
                    var isStockSplit = false
                    for (split in listOfSplits) {
                        if (split.dateInMillis == stockOrder.dateInMillis) {
                            Log.i("DeleteStockOrderDialogFragment", "Assuming stock split... Removing.")
                            // Assume that this entry is a fake stock order and really is a split
                            stockSplitApi.remove(split)
                            isStockSplit = true
                        }
                    }
                    if (!isStockSplit) {
                        val currentPortfolio = PortfolioRepository.selectedPortfolioNameStateFlow.value
                        val stockOrderApi: StockOrderApi =
                            StockOrderRepository(context, portfolioName = currentPortfolio)
                        stockOrderApi.remove(stockOrder)
                    }
                    showDeleteDialog.value = false
                }) {
                    Text(text = stringResource(id = R.string.action_delete).uppercase())
                }
            }, dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text(text = stringResource(id = android.R.string.cancel).uppercase())
                }
            })
    }

    Row(modifier = modifier
        .clickable {
            if (!readOnly) {
                showDeleteDialog.value = true
            }
        }
        .background(backgroundColor)
        .padding(8.dp)
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stockEventDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = "${stockOrder.quantity}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = nf.format(stockOrder.pricePerStock),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = nf.format(
                stockOrder.getTotalCost(
                    currencyRateApi.currencyRates.value
                        .single { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }.rateInSek
                )
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Icon(
            imageVector = (if (opinionatedStockOrder.isApprovedByAlgorithm()) {
                Icons.Outlined.SentimentSatisfied
            } else {
                Icons.Outlined.SentimentDissatisfied
            }),
            contentDescription = "Satisfaction",
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
internal fun BottomNavigation(
    currentTab: NavKey,
    onNavigationSelected: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.contentDescription("Bottom Navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
    ) {
        bottomNavigationItems.forEach { item ->
            val isSelected = currentTab == item.screenKey
            NavigationBarItem(
                icon = { NavigationItemIcon(item = item, selected = isSelected) },
                label = {
                    Text(
                        text = stringResource(item.labelResId),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = { onNavigationSelected(item.screenKey) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun NavigationItemIcon(item: NavigationItem, selected: Boolean) {
    val painter = when (item) {
        is NavigationItem.ImageVectorIcon -> rememberVectorPainter(item.iconImageVector)
    }
    val selectedPainter =  item.selectedImageVector?.let { rememberVectorPainter(it) }

    if (selectedPainter != null) {
        Crossfade(targetState = selected, label = "Navigation Crossfade") {
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
    val screenKey: NavKey,
    @param:StringRes val labelResId: Int,
    @param:StringRes val contentDescriptionResId: Int,
) {

    class ImageVectorIcon(
        screenKey: NavKey,
        @param:StringRes labelResId: Int,
        @param:StringRes contentDescriptionResId: Int,
        val iconImageVector: ImageVector,
        val selectedImageVector: ImageVector? = null,
    ) : NavigationItem(screenKey, labelResId, contentDescriptionResId)

}

private val bottomNavigationItems = listOf(
    NavigationItem.ImageVectorIcon(
        screenKey = Home,
        labelResId = R.string.home_title,
        contentDescriptionResId = R.string.home_title,
        iconImageVector = Icons.Outlined.Home,
        selectedImageVector = Icons.Filled.Home
    ),
    NavigationItem.ImageVectorIcon(
        screenKey = Dashboard,
        labelResId = R.string.dashboard_title,
        contentDescriptionResId = R.string.dashboard_title,
        iconImageVector = Icons.Outlined.Dashboard,
        selectedImageVector = Icons.Filled.Dashboard
    ),
    NavigationItem.ImageVectorIcon(
        screenKey = Notifications,
        labelResId = R.string.notifications_title,
        contentDescriptionResId = R.string.notifications_title,
        iconImageVector = Icons.Outlined.Notifications,
        selectedImageVector = Icons.Filled.Notifications
    ),
    NavigationItem.ImageVectorIcon(
        screenKey = Settings,
        labelResId = R.string.settings_title,
        contentDescriptionResId = R.string.settings_title,
        iconImageVector = Icons.Outlined.Settings,
        selectedImageVector = Icons.Filled.Settings
    )
)

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

@Composable
private fun HomeItems(
    portfolioRepository: PortfolioRepository,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onSplit: () -> Unit,
    onAddPortfolio: () -> Unit,
    homeViewModel: HomeViewModel
) {
    val portfoliosState = homeViewModel.portfoliosStateFlow.collectAsState()
    val currentPortfolioNameState = portfolioRepository.selectedPortfolioNameStateFlow.collectAsState()
    BottomSheetMenuItem(
        onClick = onBuy,
        text = stringResource(id = R.string.action_stock_buy),
        enabled = portfoliosState.value[currentPortfolioNameState.value]?.readOnly == false
    )
    BottomSheetMenuItem(
        onClick = onSell,
        text = stringResource(id = R.string.action_stock_sell),
        enabled = portfoliosState.value[currentPortfolioNameState.value]?.readOnly == false
    )
    BottomSheetMenuItem(
        onClick = onSplit,
        text = stringResource(id = R.string.action_stock_add_split),
        enabled = portfoliosState.value[currentPortfolioNameState.value]?.readOnly == false
    )
    BottomSheetMenuItem(
        onClick = onAddPortfolio,
        text = stringResource(id = R.string.action_portfolio_add)
    )
}
