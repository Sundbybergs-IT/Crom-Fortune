package com.sundbybergsit.cromfortune.main.ui.dashboard

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.main.PortfolioRepository
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.RefreshFromViewStateLaunchedEffect
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.main.ui.home.PortfolioItem
import java.math.BigDecimal
import java.time.Instant

@Composable
fun Dashboard(viewModel: DashboardViewModel, homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    LaunchedEffect(homeViewModel) {
        homeViewModel.showCurrent(context)
    }
    val viewState: StockPriceRepository.AssetViewState by StockPriceRepository.assetPricesStateFlow.collectAsState()
    RefreshFromViewStateLaunchedEffect(viewState = viewState, viewModel = viewModel)
    val scoreState = viewModel.scoreStateFlow.collectAsState()
    val portfolioSummaryState = viewModel.portfolioSummaryStateFlow.collectAsState()
    val aiStonkMood by viewModel.aiCromMood.collectAsState()
    val portfolios by homeViewModel.portfoliosStateFlow.collectAsState()
    LaunchedEffect(portfolios) {
        viewModel.refresh(context, Instant.now())
    }
    val userPortfolioValue = portfolios[PortfolioRepository.DEFAULT_PORTFOLIO_NAME]
        ?.items?.portfolioMarketValueSek() ?: BigDecimal.ZERO
    val cromPortfolio = portfolios[PortfolioRepository.CROM_PORTFOLIO_NAME]
    val cromPortfolioValue = cromPortfolio?.items
        ?.portfolioValueIncludingCashSek(cromPortfolio.cromCreditSek)
        ?: BigDecimal.ZERO
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.dashboard_title), style = MaterialTheme.typography.titleLarge)
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            ),
        )
    }) { paddingValues ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val (robotRef, scoreRef, summaryRef) = createRefs()
            createVerticalChain(robotRef, scoreRef, summaryRef, chainStyle = ChainStyle.Packed)
            AiCrom(
                modifier = Modifier
                    .constrainAs(robotRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .width(246.dp)
                    .aspectRatio(646f / 1120f),
                mood = aiStonkMood,
                userPortfolioIsWorthMore = userPortfolioValue > cromPortfolioValue
            )
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .constrainAs(scoreRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }, text = scoreState.value,
                style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                modifier = Modifier.padding(16.dp).constrainAs(summaryRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                text = portfolioSummaryState.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

internal fun List<PortfolioItem>.portfolioMarketValueSek(): BigDecimal = sumOf { item ->
    val price = StockPriceRepository.getAssetPrice(item.assetId)?.price ?: return@sumOf BigDecimal.ZERO
    val rate = CurrencyRateRepository.currencyRates.value
        .find { it.iso4217CurrencySymbol == item.currency.currencyCode }
        ?.rateInSek?.toBigDecimal() ?: BigDecimal.ONE
    item.quantity.multiply(price).multiply(rate)
}

internal fun List<PortfolioItem>.portfolioValueIncludingCashSek(cashSek: BigDecimal?): BigDecimal =
    portfolioMarketValueSek() + (cashSek ?: BigDecimal.ZERO)
