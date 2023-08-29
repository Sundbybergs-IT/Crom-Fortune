package com.sundbybergsit.cromfortune.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.sundbybergsit.cromfortune.PagerStateChangeDetectionLaunchedEffect
import com.sundbybergsit.cromfortune.PagerStateSelectionHapticFeedbackLaunchedEffect
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.stocks.StockPriceListener
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.RegisterBuyStockAlertDialog
import com.sundbybergsit.cromfortune.ui.RegisterSellStockAlertDialog
import com.sundbybergsit.cromfortune.ui.RegisterSplitStockAlertDialog
import com.sundbybergsit.cromfortune.ui.home.view.NameAndValueAdapterItem
import java.text.NumberFormat
import java.util.Currency

@Composable
fun Home(
    viewModel: HomeViewModel,
    personalStocksLiveData: State<HomeViewModel.ViewState> = viewModel.personalStocksViewState,
    cromStocksLiveData: State<HomeViewModel.ViewState> = viewModel.cromStocksViewState,
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { 2 }),
    onNavigateTo: (String) -> Unit
) {
    val localContext = LocalContext.current
    LaunchedEffect(key1 = "Test") {
        viewModel.refreshData(localContext)
    }
    RegisterBuyStockAlertDialog(
        showDialog = viewModel.showRegisterBuyStocksDialog.value,
        onDismiss = { viewModel.showRegisterBuyStocksDialog.value = false }
    ) { stockOrder ->
        viewModel.save(context = localContext, stockOrder = stockOrder)
        Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
    }
    RegisterSellStockAlertDialog(
        showDialog = viewModel.showRegisterSellStocksDialog.value,
        onDismiss = { viewModel.showRegisterSellStocksDialog.value = false },
        onSave = { stockOrder ->
            viewModel.save(context = localContext, stockOrder = stockOrder)
            Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
        },
        homeViewModel = viewModel
    )
    RegisterSplitStockAlertDialog(
        showDialog = viewModel.showRegisterSplitStocksDialog.value,
        onDismiss = { viewModel.showRegisterSplitStocksDialog.value = false },
        onSave = { stockSplit ->
            viewModel.save(context = localContext, stockSplit = stockSplit)
            Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
        }
    )
    val stockPriceListener: StockPriceListener = object : StockPriceListener {
        override fun getStockPrice(stockSymbol: String): StockPrice {
            return (StockPriceRepository.stockPrices.value as StockPriceRepository.ViewState.VALUES).stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }!!
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
        Surface {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
                    // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                    val changedPagerMutableState = remember { mutableStateOf(false) }
                    val view = LocalView.current
                    PagerStateSelectionHapticFeedbackLaunchedEffect(
                        pagerState = pagerState, view = view, changedState = changedPagerMutableState
                    )
                    HorizontalPager(modifier = Modifier.fillMaxSize(), state = pagerState) { page ->
                        PagerStateChangeDetectionLaunchedEffect(
                            pagerState = pagerState, changedPagerMutableState = changedPagerMutableState
                        )
                        val items: List<NameAndValueAdapterItem>
                        if (page == 0) {
                            items = when (personalStocksLiveData.value) {
                                is HomeViewModel.ViewState.HasStocks -> {
                                    (personalStocksLiveData.value as HomeViewModel.ViewState.HasStocks).adapterItems
                                }

                                else -> {
                                    listOf()
                                }
                            }
                            StockOrderAggregates(
                                modifier = Modifier.fillMaxSize(),
                                title = stringResource(id = R.string.home_stocks_personal_title),
                                fabActive = true,
                                viewModel = viewModel,
                                items = items,
                                stockPriceListener = stockPriceListener
                            )
                        } else {
                            items = when (cromStocksLiveData.value) {
                                is HomeViewModel.ViewState.HasStocks -> {
                                    (cromStocksLiveData.value as HomeViewModel.ViewState.HasStocks).adapterItems
                                }

                                else -> {
                                    listOf()
                                }
                            }
                            StockOrderAggregates(
                                modifier = Modifier.fillMaxSize(),
                                title = stringResource(id = R.string.home_stocks_crom_title),
                                fabActive = false,
                                viewModel = viewModel,
                                items = items,
                                stockPriceListener = stockPriceListener
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockOrderAggregates(
    modifier: Modifier, title: String, fabActive: Boolean, viewModel: HomeViewModel,
    items: List<NameAndValueAdapterItem>, stockPriceListener: StockPriceListener,
) {
    ConstraintLayout(modifier = modifier) {
        val (titleRef, listRef, fabRef) = createRefs()
        Text(modifier = Modifier.constrainAs(titleRef) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, text = title)
        Column(modifier = Modifier.constrainAs(listRef) {
            top.linkTo(titleRef.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }) {
            // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
            for (item in items) {
                if (item is StockAggregateHeaderAdapterItem) {
                    var count = 0.0
                    val currencyRates =
                        (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES).currencyRates.toList()
                    for (stockOrderAggregate in item.stockOrderAggregates.toList()) {
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
                    // FIXME: Add overflow menu with quick actions (sorting), issues/21
                } else if (item is StockAggregateAdapterItem) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                            // FIXME: Add quantity
                        }
                        val format: NumberFormat = NumberFormat.getCurrencyInstance()
                        format.currency = item.stockOrderAggregate.currency
                        format.maximumFractionDigits = 2
                        Text(
                            text = format.format(stockPriceListener.getStockPrice(item.stockOrderAggregate.stockSymbol).price),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // FIXME: Add overflow menu with quick actions (remove), issues/21
                    }
                }
            }
        }
        if (fabActive) {
            val localContext = LocalContext.current
            val showDialog = remember { mutableStateOf(false) }
            RegisterBuyStockAlertDialog(showDialog = showDialog.value, onDismiss = {
                showDialog.value = false
            }) { stockOrder ->
                viewModel.save(context = localContext, stockOrder = stockOrder)
                Toast.makeText(localContext, localContext.getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
            }
            FloatingActionButton(modifier = Modifier
                .constrainAs(fabRef) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .padding(16.dp), onClick = { showDialog.value = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Floating Action Button Icon"
                )
            }
        }
    }
}
