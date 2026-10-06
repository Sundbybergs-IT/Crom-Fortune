package com.sundbybergsit.cromfortune.main.ui.home

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.AsyncImage
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.requestUpdateFlow
import com.sundbybergsit.cromfortune.core.ui.OverflowMenu
import com.sundbybergsit.cromfortune.core.ui.PagerStateSelectionHapticFeedbackLaunchedEffect
import com.sundbybergsit.cromfortune.domain.AssetPriceApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRateApi
import com.sundbybergsit.cromfortune.main.BuildConfig
import com.sundbybergsit.cromfortune.main.DialogHandler
import com.sundbybergsit.cromfortune.main.LeafScreen
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.notes.AssetNoteRepository
import com.sundbybergsit.cromfortune.main.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.theme.Loss
import com.sundbybergsit.cromfortune.main.theme.Profit
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency

@Composable
fun Home(
    viewModel: HomeViewModel,
    assetPriceApi: AssetPriceApi = StockPriceRepository,
    onNavigateTo: (String) -> Unit,
    onOpenNotifications: () -> Unit = { onNavigateTo("notifications") },
    appUpdateManager: AppUpdateManager,
) {
    val tag = "Home"
    val localContext = LocalContext.current
    val portfoliosState by viewModel.portfoliosStateFlow.collectAsState()
    val tabs = portfoliosState.toList()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val refreshStatus = viewModel.refreshStatusStateFlow.collectAsState()
    if (!BuildConfig.DEBUG) {
        val requestUpdateFlow = remember(appUpdateManager) { appUpdateManager.requestUpdateFlow() }
        val appUpdateResultState = requestUpdateFlow.collectAsState(initial = AppUpdateResult.NotAvailable)
        val updateResult = appUpdateResultState.value
        val updateCompletedString = stringResource(R.string.generic_update_completed)
        val actionInstallString = stringResource(R.string.action_install)
        LaunchedEffect(key1 = updateResult) {
            handleInAppUpdateResult(
                updateResult = updateResult,
                downloadedMessage = updateCompletedString,
                installActionLabel = actionInstallString,
            )
        }
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.refreshAutomatically(localContext)
    }
    var expanded by remember { mutableStateOf(false) }
    val items = stringArrayResource(id = R.array.filter_array)
    var selectedIndex by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    windowInsets = WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    ),
                    actions = {
                        IconButton(onClick = onOpenNotifications) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = stringResource(R.string.notifications_title)
                            )
                        }
                        OverflowMenu(
                            onClick = { onNavigateTo(LeafScreen.BottomSheetsHome.route) },
                            contentDescription = "Home Menu",
                        )
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(onClick = { expanded = true }),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = items[selectedIndex],
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
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
                                DropdownMenuItem(
                                    onClick = {
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
                    val message = stringResource(R.string.home_information_data_refreshed)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                viewModel.refreshData(
                                    context = localContext,
                                    onFinished = { DialogHandler.showSnack(message) })
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            val updatedAt = refreshStatus.value.lastSuccessfulAt?.atZone(ZoneId.systemDefault())
                                ?.format(DateTimeFormatter.ofPattern("HH:mm"))
                            val updateStatusText = when {
                                refreshStatus.value.isRefreshing -> stringResource(R.string.home_updating)
                                refreshStatus.value.lastError != null -> updatedAt?.let {
                                    stringResource(R.string.home_update_failed_with_last_success, it)
                                } ?: stringResource(R.string.home_update_failed)
                                refreshStatus.value.failedAssets > 0 && updatedAt != null -> stringResource(
                                    R.string.home_last_updated_with_failures,
                                    updatedAt,
                                    refreshStatus.value.failedAssets
                                )
                                updatedAt != null -> stringResource(R.string.home_last_updated, updatedAt)
                                else -> stringResource(R.string.home_not_updated)
                            }
                            Text(
                                text = updateStatusText,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }) { paddingValues ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val (pagerRef, fabRef) = createRefs()
            val view = LocalView.current
            val currencyRateApi: CurrencyRateApi = CurrencyRateRepository
            val changedPagerMutableState = viewModel.changedPagerMutableStateFlow.collectAsState()
            PagerStateSelectionHapticFeedbackLaunchedEffect(
                pagerState = pagerState, view = view, changedState = changedPagerMutableState
            )
            val showFab = tabs.getOrNull(0)?.second?.items?.isEmpty() ?: false
            HorizontalPager(
                modifier = Modifier
                    .constrainAs(pagerRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxSize(),
                state = pagerState
            ) { page ->
                Column(modifier = Modifier.fillMaxSize()) {
                    SecondaryTabRow(page) {
                        val coroutineScope = rememberCoroutineScope()
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(text = title.first) },
                                selected = index == page,
                                onClick = {
                                    viewModel.selectPortfolio(title.first)
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                }
                            )
                        }
                    }
                    val portfolioName = tabs[page].first
                    val portfolioState = tabs[page].second
                    val assetNotes = AssetNoteRepository.notes.collectAsState().value
                    val currencyRates = currencyRateApi.currencyRates.collectAsState().value.toList()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "portfolio-summary-$portfolioName") {
                            StocksHeader(
                                profile = portfolioName,
                                onNavigateTo = onNavigateTo,
                                stockOrderAggregates = portfolioState.allItems,
                                assetPriceApi = assetPriceApi,
                                currencyRates = currencyRates,
                                simulatedCashBalanceSek = portfolioState.cromCreditSek,
                                sortOrder = portfolioState.sortOrder
                            )
                        }
                        itemsIndexed(
                            items = portfolioState.items,
                            key = { _, item -> item.assetId }
                        ) { _, item ->
                            AssetCard(
                                portfolioName = portfolioName,
                                item = item,
                                assetPriceApi = assetPriceApi,
                                onAssetClicked = { item, readOnly ->
                                    Log.d(
                                        tag,
                                        "Opening asset events for [${item.assetId}], readOnly=$readOnly"
                                    )
                                    if (item.assetEvents.isNotEmpty()) {
                                        DialogHandler.showAssetEvents(
                                            item = item,
                                            portfolioName = portfolioName,
                                            readOnly = readOnly
                                        )
                                    } else {
                                        DialogHandler.showStockEvents(
                                            portfolioName,
                                            item.symbol,
                                            item.legacyStockEvents,
                                            readOnly
                                        )
                                    }
                                },
                                onNavigateTo = onNavigateTo,
                                readOnly = portfolioState.readOnly,
                                noteText = assetNotes[portfolioName to item.assetId]?.text
                            )
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
            if (showFab) {
                FloatingActionButton(
                    modifier = Modifier
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
        }
    }
}

@Composable
fun StocksHeader(
    profile: String,
    onNavigateTo: (String) -> Unit,
    stockOrderAggregates: List<PortfolioItem>,
    assetPriceApi: AssetPriceApi,
    currencyRates: List<CurrencyRate>,
    simulatedCashBalanceSek: BigDecimal? = null,
    sortOrder: HomeViewModel.SortOrder = HomeViewModel.SortOrder.NAME_ASCENDING
) {
    var count = BigDecimal.ZERO
    for (stockOrderAggregate in stockOrderAggregates) {
        for (currencyRate in currencyRates) {
            if (currencyRate.iso4217CurrencySymbol == stockOrderAggregate.currency.currencyCode) {
                val assetPrice = assetPriceApi.getAssetPrice(stockOrderAggregate.assetId)
                assetPrice?.let { price ->
                    count += stockOrderAggregate.profit(price.price).multiply(currencyRate.rateInSek.toBigDecimal())
                }
                break
            }
        }
    }
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance("SEK")
    format.maximumFractionDigits = 2
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_portfolio_profit),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = format.format(count),
                    color = colorResource(
                        if (count >= BigDecimal.ZERO) R.color.colorProfit else R.color.colorLoss
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                simulatedCashBalanceSek?.let { balance ->
                    Text(
                        text = stringResource(R.string.home_cash_balance, format.format(balance)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(
                onClick = {
                    onNavigateTo(LeafScreen.BottomSheetsHomeAllStocks.createRoute(profile = profile))
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = stringResource(R.string.home_sort),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sortOrder.label(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeViewModel.SortOrder.label(): String = when (this) {
    HomeViewModel.SortOrder.NAME_ASCENDING -> stringResource(R.string.home_sort_alphabetical_down)
    HomeViewModel.SortOrder.NAME_DESCENDING -> stringResource(R.string.home_sort_alphabetical_up)
    HomeViewModel.SortOrder.PROFIT_ASCENDING -> stringResource(R.string.home_sort_profit_down)
    HomeViewModel.SortOrder.PROFIT_DESCENDING -> stringResource(R.string.home_sort_profit_up)
}

@Composable
private fun AssetCard(
    portfolioName: String,
    item: PortfolioItem,
    assetPriceApi: AssetPriceApi,
    onAssetClicked: (PortfolioItem, Boolean) -> Unit,
    onNavigateTo: (String) -> Unit,
    readOnly: Boolean,
    noteText: String?
) {
    val assetPrice = assetPriceApi.getAssetPrice(item.assetId)
    val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = item.currency
        maximumFractionDigits = 2
    }
    val priceStatuses = StockPriceRepository.assetPricesStateFlow.collectAsState().value.statuses
    val stale = priceStatuses.find { it.assetPrice.assetId == item.assetId }?.isStale == true
    val profit = assetPrice?.let { item.profit(it.price) }
    val invested = item.acquisitionValue.multiply(item.quantity)
    val stockMuteMuteSettings = StockMuteSettingsRepository.STOCK_MUTE_MUTE_SETTINGS
    val growth = if (profit == null || invested.signum() == 0) null
    else profit.divide(invested, java.math.MathContext.DECIMAL128).toDouble()
    val valueColor = when (profit?.signum()) {
        1 -> Profit
        -1 -> Loss
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onAssetClicked(item, readOnly) },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssetLogo(item = item, modifier = Modifier.size(46.dp))
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)) {
                    Text(
                        text = item.companyName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(item.symbol, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!readOnly) {
                    OverflowMenu(
                        onClick = { onNavigateTo(LeafScreen.BottomSheetsHomeStock.createRoute(
                            portfolioName = portfolioName,
                            stockSymbol = item.assetId
                        )) }
                    )
                }
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCell(
                                stringResource(R.string.home_quantity),
                                item.quantity.stripTrailingZeros().toPlainString(),
                                Modifier.weight(1f)
                            )
                            MetricCell(
                                stringResource(R.string.generic_title_latest),
                                assetPrice?.let { currencyFormat.format(it.price) + if (stale) " (stale)" else "" }
                                    ?: "—",
                                Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCell(
                                stringResource(R.string.generic_profit),
                                buildString {
                                    append(profit?.let(currencyFormat::format) ?: "—")
                                    growth?.let {
                                        appendLine()
                                        append(NumberFormat.getPercentInstance().format(it))
                                    }
                                },
                                Modifier.weight(1f),
                                color = valueColor
                            )
                            MetricCell(
                                stringResource(R.string.home_acquisition_price),
                                currencyFormat.format(item.acquisitionValue),
                                Modifier.weight(1f)
                            )
                        }
                        if (item.realizedProfit.compareTo(BigDecimal.ZERO) != 0) {
                            MetricCell(
                                stringResource(R.string.home_realized_profit),
                                currencyFormat.format(item.realizedProfit),
                                Modifier.fillMaxWidth()
                            )
                        }
                    }
            }

            if (noteText != null) {
                Text(
                    text = noteText,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!readOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TradeButton(
                        text = stringResource(R.string.home_buy),
                        color = Color(0xFF259E16),
                        modifier = Modifier.weight(1f),
                        onClick = { DialogHandler.showBuyStockDialog(stockSymbol = item.assetId) }
                    )
                    Spacer(Modifier.width(8.dp))
                    TradeButton(
                        text = stringResource(R.string.home_sell),
                        color = Color(0xFFD9293E),
                        modifier = Modifier.weight(1f),
                        onClick = { DialogHandler.showSellStockDialog(stockSymbol = item.assetId) }
                    )
                    if (item.assetType == AssetType.STOCK) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape),
                            onClick = { DialogHandler.showRegisterDividendDialog(stockSymbol = item.assetId) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AttachMoney,
                                contentDescription = stringResource(R.string.action_asset_dividend),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        val muted = stockMuteMuteSettings.collectAsState().value
                            .any { it.stockSymbol == item.symbol && it.muted }
                        IconButton(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape),
                            onClick = {
                                if (muted) StockMuteSettingsRepository.unmute(item.symbol)
                                else StockMuteSettingsRepository.mute(item.symbol)
                            }
                        ) {
                            Icon(
                                imageVector = if (muted) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications,
                                contentDescription = if (muted) "Muted stock" else "Unmuted stock"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = fontWeight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TradeButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    TextButton(
        modifier = modifier.height(42.dp),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.textButtonColors(containerColor = color, contentColor = Color.White)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

private fun PortfolioItem.companyName(): String = displayName
    .removePrefix("[CRYPTO] ")
    .removeSuffix(" ($symbol)")

private fun PortfolioItem.assetInitials(): String = symbol
    .filter(Char::isLetterOrDigit)
    .take(3)
    .uppercase()

@Composable
private fun AssetLogo(item: PortfolioItem, modifier: Modifier = Modifier) {
    val token = BuildConfig.LOGO_DEV_PUBLISHABLE_KEY
    val logoUrl = if (token.isBlank()) null else {
        val path = when (item.assetType) {
            AssetType.CRYPTO -> "crypto/${Uri.encode(item.symbol)}"
            AssetType.STOCK -> "ticker/${Uri.encode(item.symbol)}"
        }
        val theme = if (isSystemInDarkTheme()) "dark" else "light"
        "https://img.logo.dev/$path" +
            "?token=${Uri.encode(token)}&size=96&format=png&theme=$theme&fallback=404"
    }
    var logoLoaded by remember(logoUrl) { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (logoLoaded) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!logoLoaded) {
            Text(
                text = item.assetInitials(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = stringResource(R.string.home_asset_logo, item.companyName()),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                contentScale = ContentScale.Fit,
                onSuccess = { logoLoaded = true },
                onError = { logoLoaded = false }
            )
        }
    }
}
