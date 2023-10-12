package com.sundbybergsit.cromfortune.main.ui.home

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
import androidx.compose.material.IconButton
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.collectAsState
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
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPriceApi
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRateApi
import com.sundbybergsit.cromfortune.main.DialogHandler
import com.sundbybergsit.cromfortune.main.LeafScreen
import com.sundbybergsit.cromfortune.main.OverflowMenu
import com.sundbybergsit.cromfortune.main.PagerStateSelectionHapticFeedbackLaunchedEffect
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.theme.Loss
import com.sundbybergsit.cromfortune.main.theme.Profit
import java.text.NumberFormat
import java.util.Currency

@Composable
fun Home(
    viewModel: HomeViewModel,
    pagerState: PagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { viewModel.portfoliosStateFlow.value.size }),
    stockPriceApi: StockPriceApi = StockPriceRepository,
    onNavigateTo: (String) -> Unit,
) {
    val localContext = LocalContext.current
    val portfoliosState = viewModel.portfoliosStateFlow.collectAsState()
    LaunchedEffect(key1 = Unit) {
        viewModel.refreshData(localContext)
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
                    .width(164.dp)
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
                            } else {
                                viewModel.showAll(context = localContext)
                            }
                        },
                            text = {
                                Text(text = label)
                            }
                        )
                    }
                }
            }
            IconButton(onClick = {
                viewModel.refreshData(
                    context = localContext,
                    onFinished = { DialogHandler.showSnack(localContext.getString(R.string.home_information_data_refreshed)) })
            }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onSurface
                )
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
            val showFabMutableState = remember { mutableStateOf(false) }
            val currencyRateApi: CurrencyRateApi = CurrencyRateRepository
            if (showFabMutableState.value) {
                FloatingActionButton(modifier = Modifier
                    .constrainAs(fabRef) {
                        end.linkTo(parent.end, 16.dp)
                        bottom.linkTo(parent.bottom, 32.dp)
                    }
                    .padding(16.dp), onClick = { DialogHandler.showBuyStockDialog() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Floating Action Button Icon"
                    )
                }
            }
            val changedPagerMutableState = viewModel.changedPagerMutableStateFlow.collectAsState()
            PagerStateSelectionHapticFeedbackLaunchedEffect(
                pagerState = pagerState, view = view, changedState = changedPagerMutableState
            )
            val mutableTabs: MutableList<Pair<String, HomeViewModel.ViewState>> = mutableListOf()
            for (entry in portfoliosState.value.entries) {
                mutableTabs.add(entry.key to entry.value)
            }
            val tabs: List<Pair<String, HomeViewModel.ViewState>> = mutableTabs.toList()
            showFabMutableState.value = tabs[0].second.items.isEmpty()
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
                                        viewModel.selectTab(
                                            portfolioName = title.first,
                                            index = index,
                                            pagerState = pagerState,
                                            coroutineScope = coroutineScope
                                        )
                                    }
                                )
                            }
                        }
                    }
                    LazyColumn {
                        items(count = tabs[pagerState.currentPage].second.items.size) { lazyItemScope ->
                            val portfolioName = tabs[pagerState.currentPage].first
                            val portfolioState = portfoliosState.value[portfolioName]!!
                            StocksTab(
                                portfolioName = portfolioName,
                                index = lazyItemScope,
                                viewState = portfolioState,
                                stockPriceApi = stockPriceApi,
                                onShowStock = { stockSymbol, readOnly ->
                                    DialogHandler.showStockEvents(
                                        stockSymbol = stockSymbol, stockEvents = viewModel.portfolioStockEvents(
                                            portfolioName = portfolioName,
                                            context = localContext,
                                            stockSymbol = stockSymbol
                                        ), readOnly = readOnly
                                    )
                                },
                                onNavigateTo = onNavigateTo,
                                readOnly = portfolioState.readOnly,
                                currencyRateApi = currencyRateApi
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StocksHeader(
    profile: String,
    onNavigateTo: (String) -> Unit,
    stockOrderAggregates: List<StockOrderAggregate>,
    stockPriceApi: StockPriceApi,
    currencyRates: List<CurrencyRate>
) {
    var count = 0.0
    for (stockOrderAggregate in stockOrderAggregates) {
        for (currencyRate in currencyRates) {
            if (currencyRate.iso4217CurrencySymbol == stockOrderAggregate.currency.currencyCode) {
                count += (stockOrderAggregate.getProfit(
                    stockPriceApi.getStockPrice(
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
    portfolioName: String,
    index: Int,
    viewState: HomeViewModel.ViewState,
    stockPriceApi: StockPriceApi,
    onShowStock: (String, Boolean) -> Unit,
    onNavigateTo: (String) -> Unit,
    readOnly: Boolean,
    currencyRateApi: CurrencyRateApi
) {
    val currencyRatesState = currencyRateApi.currencyRates.collectAsState()
    if (index == 0) {
        val currencyRates = currencyRatesState.value.toList()
        StocksHeader(
            profile = portfolioName,
            onNavigateTo = onNavigateTo,
            stockOrderAggregates = viewState.items,
            stockPriceApi = stockPriceApi,
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
        if (!readOnly) {
            OverflowMenu(
                onNavigateTo = onNavigateTo,
                route = LeafScreen.BottomSheetsHomeStock.createRoute(
                    portfolioName = portfolioName,
                    stockSymbol = viewState.items[index].stockSymbol
                )
            )
        }
    }
    StockOrderAggregateItem(
        item = viewState.items[index],
        stockPriceApi = stockPriceApi,
        onShowStock = onShowStock,
        readOnly = readOnly
    )
}

@Composable
private fun StockOrderAggregateItem(
    item: StockOrderAggregate, stockPriceApi: StockPriceApi,
    onShowStock: (String, Boolean) -> Unit,
    readOnly: Boolean
) {
    val stockPrice = stockPriceApi.getStockPrice(item.stockSymbol)
    val profit = item.getProfit(stockPrice.price)
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.currency = item.currency
    format.maximumFractionDigits = 2
    Surface(modifier = Modifier.clickable { onShowStock.invoke(item.stockSymbol, readOnly) }) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = item.getQuantity().toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = format.format(item.getAcquisitionValue()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
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
            if (!readOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp), horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            DialogHandler.showBuyStockDialog(stockSymbol = item.stockSymbol)
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
                        onClick = { DialogHandler.showSellStockDialog(stockSymbol = item.stockSymbol) },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = colorResource(
                                id = (android.R.color.holo_red_dark)
                            )
                        )
                    ) {
                        Text(text = stringResource(id = R.string.action_stock_sell_short))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    if (StockMuteSettingsRepository.STOCK_MUTE_MUTE_SETTINGS.value
                            .find { stockMuteSettings -> stockMuteSettings.stockSymbol == item.stockSymbol && stockMuteSettings.muted } != null
                    ) {
                        IconButton(onClick = {
                            StockMuteSettingsRepository.unmute(item.stockSymbol)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsOff,
                                contentDescription = "Muted stock",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            StockMuteSettingsRepository.mute(item.stockSymbol)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "Unmuted stock",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}
