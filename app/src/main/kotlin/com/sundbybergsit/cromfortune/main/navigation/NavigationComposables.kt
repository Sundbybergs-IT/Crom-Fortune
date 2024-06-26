package com.sundbybergsit.cromfortune.main.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomNavigationItem
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.android.play.core.appupdate.AppUpdateManager
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
import com.sundbybergsit.cromfortune.main.LeafScreen
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

private const val DATE_FORMAT = "MM/dd/yyyy"

@Composable
internal fun AppNavigation(navController: NavHostController, portfolioRepository: PortfolioRepository) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator
    AddDialogs(dialogHandler = DialogHandler, portfolioRepository = portfolioRepository)
    ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
        val snackbarHostState = remember { SnackbarHostState() }
        val view = LocalView.current
        ShowSnackbarLaunchedEffect(
            dialogHandler = DialogHandler,
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
            val context = LocalContext.current
            Box(Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = { defaultCromEnterTransition(initialState, targetState) },
                    exitTransition = { defaultCromExitTransition(initialState, targetState) },
                    popEnterTransition = { defaultCromPopEnterTransition() },
                    popExitTransition = { defaultCromPopExitTransition() },
                ) {
                    val appUpdateManager = AppUpdateManagerFactory.create(context)
                    addHomeTopLevel(navController = navController, portfolioRepository = portfolioRepository, appUpdateManager=appUpdateManager)
                    addDashboardTopLevel(navController = navController, portfolioRepository = portfolioRepository, appUpdateManager=appUpdateManager)
                    addNotificationsTopLevel(navController = navController, portfolioRepository = portfolioRepository, appUpdateManager=appUpdateManager)
                    addSettingsTopLevel(navController = navController, portfolioRepository = portfolioRepository, appUpdateManager=appUpdateManager)
                }
            }
        }
    }
}

@Composable
fun AddDialogs(
    dialogHandler: DialogHandler,
    portfolioRepository: PortfolioRepository,
    selectedDaysMutableState: MutableState<Set<DayOfWeek>> = remember { mutableStateOf(setOf()) },
    fromTimePickerState: MutableState<TimePickerState?> = remember { mutableStateOf(null) },
    toTimePickerState: MutableState<TimePickerState?> = remember { mutableStateOf(null) }
) {
    val portfolioNameState = portfolioRepository.selectedPortfolioNameStateFlow.collectAsState()
    when (val dialogViewState = dialogHandler.dialogViewState.collectAsState().value) {
        is DialogHandler.DialogViewState.ShowDeleteDialog -> {
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
                        text = stringResource(id = R.string.home_delete_all_stock_orders, dialogViewState.stockName),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                onDismissRequest = { dialogHandler.dismissDialog() },
                confirmButton = {
                    TextButton(onClick = {
                        dialogViewState.onClickRemove(context = context, stockSymbol = dialogViewState.stockName)
                        dialogHandler.dismissDialog()
                    }) {
                        Text(stringResource(id = R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        dialogHandler.dismissDialog()
                    }) {
                        Text(stringResource(id = R.string.action_close))
                    }
                }
            )
        }

        is DialogHandler.DialogViewState.ShowStockRetrievalTimeIntervalsDialog -> {
            val settingsViewState by dialogViewState.stockRetrievalSettings.timeInterval
            UpdateTimePickerLaunchedEffect(
                settingsViewState, dialogViewState, selectedDaysMutableState,
                fromTimePickerState, toTimePickerState
            )
            val context = LocalContext.current
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
                                    selectedDaysMutableState.value = selectedDaysMutableState.value - day
                                } else {
                                    selectedDaysMutableState.value = selectedDaysMutableState.value + day
                                }
                            })
                        }
                    },
                    onDismissRequest = { dialogHandler.dismissDialog() },
                    confirmButton = {
                        TextButton(onClick = {
                            val nullSafeFromTimePickerState = checkNotNull(fromTimePickerState.value)
                            val nullSafeToTimePickerState = checkNotNull(toTimePickerState.value)
                            dialogViewState.stockRetrievalSettings.set(nullSafeFromTimePickerState.hour,
                                nullSafeFromTimePickerState.minute,
                                nullSafeToTimePickerState.hour,
                                nullSafeToTimePickerState.minute,
                                selectedDaysMutableState.value.map { materialWeekday ->
                                    DayOfWeek.valueOf(
                                        materialWeekday.name
                                    )
                                })
                            dialogHandler.dismissDialog()
                            dialogHandler.showSnack(context.getString(R.string.generic_saved))
                        }) {
                            Text(stringResource(id = android.R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            dialogHandler.dismissDialog()
                        }) {
                            Text(stringResource(id = android.R.string.cancel))
                        }
                    }
                )
            }
        }

        is DialogHandler.DialogViewState.ShowSupportedStocksDialog -> {
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
                        text = dialogViewState.text,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                onDismissRequest = { dialogHandler.dismissDialog() },
                confirmButton = {
                    TextButton(onClick = {
                        dialogHandler.dismissDialog()
                    }) {
                        Text(stringResource(id = android.R.string.ok))
                    }
                }
            )
        }

        DialogHandler.DialogViewState.Dismissed -> {
            // Do nothing
        }

        is DialogHandler.DialogViewState.ShowStockEvents -> {
            val context = LocalContext.current
            val stockEvents = dialogViewState.stockEvents
            val opinionatedEvents: List<OpinionatedStockOrderWrapper> = getOpinionatedStockOrders(
                stockEvents,
                CromFortuneV1RecommendationAlgorithm(context)
            )
            Dialog(onDismissRequest = { dialogHandler.dismissDialog() }) {
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
                                            text = dialogViewState.title,
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
                                        readOnly = dialogViewState.readOnly
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
                            onClick = {
                                dialogHandler.dismissDialog()
                            }
                        ) {
                            Text(stringResource(id = R.string.action_close).uppercase())
                        }
                    }
                }
            }
        }

        is DialogHandler.DialogViewState.ShowBuyStockDialog -> {
            val localContext = LocalContext.current
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
                Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
            }
        }

        is DialogHandler.DialogViewState.ShowSellStockDialog -> {
            val localContext = LocalContext.current
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
                    Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT)
                        .show()
                },
                homeViewModel = homeViewModel,
                portfolioRepository = portfolioRepository,
            )
        }

        is DialogHandler.DialogViewState.ShowRegisterSplitStockDialog -> {
            val localContext = LocalContext.current
            val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                HomeViewModelFactory(portfolioRepository = portfolioRepository)
            })
            RegisterSplitStockAlertDialog(
                portfolioNameState = portfolioNameState,
                onDismiss = { DialogHandler.dismissDialog() }, stockSymbolParam = dialogViewState.stockSymbol
            ) { stockSplit ->
                homeViewModel.save(context = localContext, stockSplit = stockSplit)
                Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
            }
        }

        is DialogHandler.DialogViewState.ShowAddPortfolio -> {
            val localContext = LocalContext.current
            val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
                HomeViewModelFactory(portfolioRepository = portfolioRepository)
            })
            PortfolioAddAlertDialog(onDismiss = { DialogHandler.dismissDialog() }) { portfolioName ->
                homeViewModel.savePortfolio(context = localContext, portfolioName = portfolioName)
                Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
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

private fun NavGraphBuilder.addHomeTopLevel(
    navController: NavHostController,
    portfolioRepository: PortfolioRepository,
    appUpdateManager: AppUpdateManager
) {
    addHome(navController = navController, portfolioRepository = portfolioRepository,appUpdateManager=appUpdateManager)
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
    addHomeBottomSheet(portfolioRepository = portfolioRepository)
    addHomeAllStocksBottomSheet(portfolioRepository = portfolioRepository)
    addHomeStockBottomSheet()
}

private fun NavGraphBuilder.addDashboardTopLevel(
    navController: NavHostController,
    portfolioRepository: PortfolioRepository,
    appUpdateManager: AppUpdateManager
) {
    addHome(
        navController = navController,
        portfolioRepository = portfolioRepository,
        appUpdateManager = appUpdateManager
    )
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
}

private fun NavGraphBuilder.addNotificationsTopLevel(
    navController: NavHostController,
    portfolioRepository: PortfolioRepository,
    appUpdateManager: AppUpdateManager
) {
    addHome(
        navController = navController,
        portfolioRepository = portfolioRepository,
        appUpdateManager = appUpdateManager
    )
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
    addNotificationsBottomSheet()
}

private fun NavGraphBuilder.addSettingsTopLevel(
    navController: NavHostController,
    portfolioRepository: PortfolioRepository,
    appUpdateManager: AppUpdateManager
) {
    addHome(
        navController = navController,
        portfolioRepository = portfolioRepository,
        appUpdateManager = appUpdateManager
    )
    addDashboard(navController = navController)
    addNotifications(navController = navController)
    addSettings(navController = navController)
    addSettingsBottomSheet()
}

private fun NavGraphBuilder.addHome(
    navController: NavHostController,
    portfolioRepository: PortfolioRepository,
    appUpdateManager: AppUpdateManager
) {
    composable(route = Screen.Home.route) {
        val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
            HomeViewModelFactory(portfolioRepository = portfolioRepository)
        })
        Home(viewModel = homeViewModel, onNavigateTo = { route -> navController.navigate(route) }, appUpdateManager=appUpdateManager)
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

private fun NavGraphBuilder.addHomeBottomSheet(portfolioRepository: PortfolioRepository) {
    bottomSheet(route = LeafScreen.BottomSheetsHome.route) {
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
}

private fun NavGraphBuilder.addHomeAllStocksBottomSheet(portfolioRepository: PortfolioRepository) {
    bottomSheet(
        route = LeafScreen.BottomSheetsHomeAllStocks.route,
        arguments = listOf(
            navArgument("profile") {
                type = NavType.StringType
                nullable = false
            }),
    ) { backStackEntry ->
        val arguments = checkNotNull(backStackEntry.arguments)
        val profile = checkNotNull(arguments.getString("profile"))
        val homeViewModel: HomeViewModel by activityBoundViewModel(factoryProducer = {
            HomeViewModelFactory(portfolioRepository = portfolioRepository)
        })
        val onSortNameAscending = { homeViewModel.sortNameAscending(profile) }
        val onSortNameDescending = { homeViewModel.sortNameDescending(profile) }
        val onSortProfitAscending = { homeViewModel.sortProfitAscending(profile) }
        val onSortProfitDescending = { homeViewModel.sortProfitDescending(profile) }
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
}

private fun NavGraphBuilder.addHomeStockBottomSheet() {
    bottomSheet(
        route = LeafScreen.BottomSheetsHomeStock.route,
        arguments = listOf(
            navArgument("portfolio_name") {
                type = NavType.StringType
                nullable = false
            },
            navArgument("stock_symbol") {
                type = NavType.StringType
                nullable = true
            }),
    ) { backStackEntry ->
        val arguments = checkNotNull(backStackEntry.arguments)
        val stockSymbol = checkNotNull(arguments.getString("stock_symbol"))
        val portfolioName = checkNotNull(arguments.getString("portfolio_name"))
        val context = LocalContext.current
        val onDelete =
            {
                DialogHandler.showDeleteDialog(
                    context = context,
                    portfolioName = portfolioName,
                    stockName = stockSymbol
                )
            }
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
        BottomSheetContent {
            SettingsItems(
                onShowSupportedStocks = { DialogHandler.showSupportedStocksDialog() },
                onShowStockRetrievalTimeIntervals = {
                    DialogHandler.showStockRetrievalTimeIntervalsDialog(StockRetrievalSettings(context))
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
