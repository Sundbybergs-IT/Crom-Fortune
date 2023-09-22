package com.sundbybergsit.cromfortune.ui.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.DialogHandler
import com.sundbybergsit.cromfortune.LeafScreen
import com.sundbybergsit.cromfortune.OverflowMenu
import com.sundbybergsit.cromfortune.PagerStateSelectionHapticFeedbackLaunchedEffect
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.stocks.StockPriceListener
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.theme.Loss
import com.sundbybergsit.cromfortune.theme.Profit
import com.sundbybergsit.cromfortune.ui.RegisterBuyStockAlertDialog
import com.sundbybergsit.cromfortune.ui.RegisterSellStockAlertDialog
import com.sundbybergsit.cromfortune.ui.RegisterSplitStockAlertDialog
import java.text.NumberFormat
import java.util.Currency

@Composable
fun Home(
    viewModel: HomeViewModel,
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { 2 }),
    onNavigateTo: (String) -> Unit
) {
    val localContext = LocalContext.current
    val personalStocksViewState: HomeViewModel.ViewState by viewModel.personalStocksViewState
    val cromStocksViewState: HomeViewModel.ViewState by viewModel.cromStocksViewState
    val showRegisterBuyDialog by viewModel.showRegisterBuyStocksDialog
    val showRegisterSellDialog by viewModel.showRegisterSellStocksDialog
    val showRegisterSplitDialog by viewModel.showRegisterSplitStocksDialog
    val stockPricesViewState: StockPriceRepository.ViewState? by StockPriceRepository.stockPrices
    LaunchedEffect(key1 = Unit) {
        viewModel.refreshData(localContext)
    }
    RegisterBuyStockAlertDialog(
        showDialog = showRegisterBuyDialog,
        onDismiss = { viewModel.showRegisterBuyStocksDialog.value = false }
    ) { stockOrder ->
        viewModel.save(context = localContext, stockOrder = stockOrder)
        Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
    }
    RegisterSellStockAlertDialog(
        showDialog = showRegisterSellDialog,
        onDismiss = { viewModel.showRegisterSellStocksDialog.value = false },
        onSave = { stockOrder ->
            viewModel.save(context = localContext, stockOrder = stockOrder)
            Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
        },
        homeViewModel = viewModel
    )
    RegisterSplitStockAlertDialog(
        showDialog = showRegisterSplitDialog,
        onDismiss = { viewModel.showRegisterSplitStocksDialog.value = false },
        onSave = { stockSplit ->
            viewModel.save(context = localContext, stockSplit = stockSplit)
            Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
        }, homeViewModel = viewModel
    )
    val stockPriceListener: StockPriceListener = object : StockPriceListener {
        override fun getStockPrice(stockSymbol: String): StockPrice {
            return checkNotNull(stockPricesViewState).stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }!!
        }
    }
    var expanded by remember { mutableStateOf(false) }
    val items = stringArrayResource(id = R.array.filter_array)
    var selectedIndex by remember { mutableIntStateOf(0) }
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.home_title), style = MaterialTheme.typography.titleMedium)
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ), actions = {
            Box(
                modifier = Modifier
                    .width(178.dp)
                    .padding(16.dp)
                    .clickable(onClick = { expanded = true }),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = items[selectedIndex], modifier = Modifier.padding(16.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown arrow",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items.forEachIndexed { index, label ->
                        DropdownMenuItem(onClick = {
                            selectedIndex = index
                            expanded = false
                            if (selectedIndex == 0) {
                                viewModel.showCurrent(localContext)
                            } else if (selectedIndex == 1) {
                                viewModel.showAll(localContext)
                            }
                        },
                            text = {
                                Text(text = label)
                            }
                        )
                    }
                }
            }
            TextButton(onClick = {
                viewModel.refreshData(
                    context = localContext,
                    onFinished = { DialogHandler.showSnack(localContext.getString(R.string.home_information_data_refreshed)) })
            }) {
                Text(text = stringResource(id = R.string.action_refresh).uppercase())
            }
            OverflowMenu(
                onNavigateTo = onNavigateTo, contentDescription = "Home Menu",
                route = LeafScreen.BottomSheetsHome.route
            )
        })
    }) { paddingValues ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val (pagerRef, fabRef) = createRefs()
            val view = LocalView.current
            val showBuyDialogMutableState = remember { mutableStateOf(false) }
            val showFabMutableState = remember { mutableStateOf(false) }
            RegisterBuyStockAlertDialog(showDialog = showBuyDialogMutableState.value, onDismiss = {
                showBuyDialogMutableState.value = false
            }) { stockOrder ->
                viewModel.save(context = localContext, stockOrder = stockOrder)
                Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
            }
            if (showFabMutableState.value) {
                FloatingActionButton(modifier = Modifier
                    .constrainAs(fabRef) {
                        end.linkTo(parent.end, 16.dp)
                        bottom.linkTo(parent.bottom, 32.dp)
                    }
                    .padding(16.dp), onClick = { showBuyDialogMutableState.value = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Floating Action Button Icon"
                    )
                }
            }
            PagerStateSelectionHapticFeedbackLaunchedEffect(
                pagerState = pagerState, view = view, changedState = viewModel.changedPagerMutableState
            )
            val tabs = listOf(
                stringResource(id = R.string.home_stocks_personal_title).uppercase() to viewModel.personalStocksViewState,
                stringResource(id = R.string.home_stocks_crom_title).uppercase() to viewModel.cromStocksViewState,
            )
            showFabMutableState.value = tabs[0].second.value.items.isEmpty()
            HorizontalPager(
                modifier = Modifier.constrainAs(pagerRef) {},
                state = pagerState
            ) {
                Column {
                    TabRow(pagerState.currentPage) {
                        val coroutineScope = rememberCoroutineScope()
                        tabs.forEachIndexed { index, title ->
                            Column {
                                Tab(
                                    text = { Text(text = title.first) },
                                    selected = index == pagerState.currentPage,
                                    onClick = {
                                        viewModel.selectTab(index, pagerState, coroutineScope)
                                    }
                                )
                            }
                        }
                    }
                    LazyColumn {
                        items(count = tabs[pagerState.currentPage].second.value.items.size) { lazyItemScope ->
                            when (pagerState.currentPage) {
                                0 -> StocksTab(
                                    profile = "personal",
                                    index = lazyItemScope,
                                    viewState = personalStocksViewState,
                                    stockPriceListener = stockPriceListener,
                                    onShowStock = { stockSymbol ->
                                        DialogHandler.showStockEvents(
                                            stockSymbol, viewModel.personalStockEvents(
                                                context = localContext,
                                                stockSymbol = stockSymbol
                                            )
                                        )
                                    },
                                    onNavigateTo = onNavigateTo
                                )

                                1 -> StocksTab(
                                    profile = "crom",
                                    index = lazyItemScope,
                                    viewState = cromStocksViewState,
                                    stockPriceListener = stockPriceListener,
                                    onShowStock = { stockSymbol ->
                                        DialogHandler.showStockEvents(
                                            stockSymbol, viewModel.cromStockEvents(
                                                context = localContext,
                                                stockSymbol = stockSymbol
                                            )
                                        )
                                    },
                                    onNavigateTo = onNavigateTo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StocksHeader(
    profile : String,
    onNavigateTo: (String) -> Unit,
    stockOrderAggregates: List<StockOrderAggregate>,
    stockPriceListener: StockPriceListener,
    currencyRates: List<CurrencyRate>
) {
    var count = 0.0
    for (stockOrderAggregate in stockOrderAggregates) {
        for (currencyRate in currencyRates) {
            if (currencyRate.iso4217CurrencySymbol == stockOrderAggregate.currency.currencyCode) {
                count += (stockOrderAggregate.getProfit(
                    stockPriceListener.getStockPrice(
                        stockOrderAggregate.stockSymbol
                    ).price
                )) * currencyRate.rateInSek
                break
            }
        }
    }
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance("SEK")
    format.maximumFractionDigits = 2
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .width(IntrinsicSize.Max)
        ) {
            Text(
                text = "#", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .width(IntrinsicSize.Max)
        ) {
            Text(
                text = stringResource(id = R.string.generic_acquisition_value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .width(IntrinsicSize.Max)
        ) {
            Text(
                text = stringResource(id = R.string.generic_title_latest), style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .width(IntrinsicSize.Max)
        ) {
            Text(
                text = stringResource(id = R.string.generic_profit), style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        OverflowMenu(
            onNavigateTo = onNavigateTo, contentDescription = "Home All Stocks Menu",
            route = LeafScreen.BottomSheetsHomeAllStocks.createRoute(profile = profile)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(3f)
                .width(IntrinsicSize.Max)
        ) {
            // Nothing
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .width(IntrinsicSize.Max)
        ) {
            Text(
                text = format.format(count), color = colorResource(
                    if (count >= 0.0) {
                        R.color.colorProfit
                    } else {
                        R.color.colorLoss
                    }
                ), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StocksTab(
    profile : String,
    index: Int,
    viewState: HomeViewModel.ViewState,
    stockPriceListener: StockPriceListener,
    onShowStock: (String) -> Unit,
    onNavigateTo: (String) -> Unit,
) {
    if (index == 0) {
        val currencyRates =
            CurrencyRateRepository.currencyRates.value?.currencyRates?.toList() ?: listOf()
        StocksHeader(
            profile = profile,
            onNavigateTo = onNavigateTo,
            stockOrderAggregates = viewState.items,
            stockPriceListener = stockPriceListener,
            currencyRates = currencyRates
        )
    }
    Divider(thickness = 1.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = viewState.items[index].displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        OverflowMenu(
            onNavigateTo = onNavigateTo,
            route = LeafScreen.BottomSheetsHomeStock.createRoute(viewState.items[index].stockSymbol)
        )
    }
    StockOrderAggregateItem(
        item = viewState.items[index],
        stockPriceListener = stockPriceListener,
        onShowStock = onShowStock
    )
}

@Composable
private fun StockOrderAggregateItem(
    item: StockOrderAggregate, stockPriceListener: StockPriceListener,
    onShowStock: (String) -> Unit
) {
    val stockPrice = stockPriceListener.getStockPrice(item.stockSymbol)
    val profit = item.getProfit(stockPrice.price)
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.currency = item.currency
    format.maximumFractionDigits = 2
    val localContext = LocalContext.current
    Surface(modifier = Modifier.clickable { onShowStock.invoke(item.stockSymbol) }) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(text = item.getQuantity().toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(text = format.format(item.getAcquisitionValue()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = format.format(stockPrice.price),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = format.format(profit),
                        style = MaterialTheme.typography.bodySmall,
                        color = when (profit.compareTo(0)) {
                            1 -> Profit
                            -1 -> Loss
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp), horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        // FIXME: Show buy dialog, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                        DialogHandler.showSnack(localContext.getString(R.string.generic_error_not_supported))
                    }, colors = ButtonDefaults.textButtonColors(
                        backgroundColor = colorResource(
                            id = (android.R.color.holo_green_dark)
                        )
                    )
                ) {
                    Text(text = stringResource(id = R.string.action_stock_buy_short))
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    onClick = {
                        // FIXME: Show sell dialog, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                        DialogHandler.showSnack(localContext.getString(R.string.generic_error_not_supported))
                    }, colors = ButtonDefaults.textButtonColors(
                        backgroundColor = colorResource(
                            id = (android.R.color.holo_red_dark)
                        )
                    )
                ) {
                    Text(text = stringResource(id = R.string.action_stock_sell_short))
                }
                Spacer(modifier = Modifier.width(16.dp))
                if (StockMuteSettingsRepository.isMuted(item.stockSymbol)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fas_bell_slash),
                        contentDescription = "Muted stock"
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fas_bell),
                        contentDescription = "Unmuted stock"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}
