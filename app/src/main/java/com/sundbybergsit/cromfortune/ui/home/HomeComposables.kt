package com.sundbybergsit.cromfortune.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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
    LaunchedEffect(key1 = "Test") {
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
        }
    )
    val stockPriceListener: StockPriceListener = object : StockPriceListener {
        override fun getStockPrice(stockSymbol: String): StockPrice {
            return checkNotNull(stockPricesViewState).stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }!!
        }
    }
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.home_title), style = MaterialTheme.typography.titleMedium)
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ), actions = {
            OverflowMenu(
                onNavigateTo = onNavigateTo, contentDescription = "Notifications Menu",
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
            val localContext = LocalContext.current
            val showBuyDialog = remember { mutableStateOf(false) }
            RegisterBuyStockAlertDialog(showDialog = showBuyDialog.value, onDismiss = {
                showBuyDialog.value = false
            }) { stockOrder ->
                viewModel.save(context = localContext, stockOrder = stockOrder)
                Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
            }
            FloatingActionButton(modifier = Modifier
                .constrainAs(fabRef) {
                    end.linkTo(parent.end, 16.dp)
                    bottom.linkTo(parent.bottom, 32.dp)
                }
                .padding(16.dp), onClick = { showBuyDialog.value = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Floating Action Button Icon"
                )
            }
            PagerStateSelectionHapticFeedbackLaunchedEffect(
                pagerState = pagerState, view = view, changedState = viewModel.changedPagerMutableState
            )
            val tabs = listOf(
                stringResource(id = R.string.home_stocks_personal_title) to viewModel.personalStocksViewState,
                stringResource(id = R.string.home_stocks_crom_title) to viewModel.cromStocksViewState,
            )
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
                                    index = lazyItemScope,
                                    viewState = personalStocksViewState,
                                    stockPriceListener = stockPriceListener
                                )

                                1 -> StocksTab(
                                    index = lazyItemScope,
                                    viewState = cromStocksViewState,
                                    stockPriceListener = stockPriceListener
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
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Total profit: " + format.format(count), color = colorResource(
                if (count >= 0.0) {
                    R.color.colorProfit
                } else {
                    R.color.colorLoss
                }
            )
        )
    }
}

@Composable
private fun StocksTab(
    index: Int,
    viewState: HomeViewModel.ViewState,
    stockPriceListener: StockPriceListener
) {
    if (index == 0) {
        val currencyRates =
            CurrencyRateRepository.currencyRates.value?.currencyRates?.toList() ?: listOf()
        StocksHeader(
            viewState.items,
            stockPriceListener,
            currencyRates
        )
    }
    StockOrderAggregateItem(
        item = viewState.items[index],
        stockPriceListener = stockPriceListener
    )
}

@Composable
private fun StockOrderAggregateItem(
    item: StockOrderAggregate, stockPriceListener: StockPriceListener,
) {
    // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
    Row(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(text = item.displayName, style = MaterialTheme.typography.bodyMedium)
            // FIXME: Add quantity
        }
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.currency = item.currency
        format.maximumFractionDigits = 2
        Text(
            text = format.format(stockPriceListener.getStockPrice(item.stockSymbol).price),
            style = MaterialTheme.typography.bodyMedium
        )
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

        // FIXME: Add overflow menu with quick actions (remove), issues/21
    }
}
