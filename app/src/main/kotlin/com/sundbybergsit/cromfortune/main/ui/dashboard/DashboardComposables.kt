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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.RefreshFromViewStateLaunchedEffect
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository

@Composable
fun Dashboard(viewModel: DashboardViewModel) {
    val viewState: StockPriceRepository.AssetViewState by StockPriceRepository.assetPricesStateFlow.collectAsState()
    RefreshFromViewStateLaunchedEffect(viewState = viewState, viewModel = viewModel)
    val scoreState = viewModel.scoreStateFlow.collectAsState()
    val portfolioSummaryState = viewModel.portfolioSummaryStateFlow.collectAsState()
    val aiStonkMood by viewModel.aiStonkMood.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.dashboard_title), style = MaterialTheme.typography.titleMedium)
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
            AiStonk(
                mood = aiStonkMood,
                modifier = Modifier
                    .constrainAs(robotRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .width(246.dp)
                    .aspectRatio(646f / 840f)
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
