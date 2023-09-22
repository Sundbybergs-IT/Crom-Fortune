package com.sundbybergsit.cromfortune.main.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository

@Composable
fun Dashboard(viewModel: DashboardViewModel, onBack: () -> Unit) {
    val viewState: StockPriceRepository.ViewState? by StockPriceRepository.stockPrices
    val context = LocalContext.current
    LaunchedEffect(key1 = "RetrieveStocksLaunchedEffect") {
        when (viewState) {
            is StockPriceRepository.ViewState -> {
                viewModel.refresh(
                    context = context,
                    timestamp = checkNotNull(viewState).instant,
                    stockPrices = checkNotNull(viewState).stockPrices
                )
            }

            else -> {
                // Do nothing
            }
        }
    }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.dashboard_title), style = MaterialTheme.typography.titleMedium)
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            }
        )
    }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val (imageRef, scoreRef) = createRefs()
            createVerticalChain(imageRef, scoreRef, chainStyle = ChainStyle.Packed)
            Image(
                modifier = Modifier
                    .constrainAs(imageRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(160.dp),
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.stonks),
                contentDescription = "Stonk Image"
            )
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .constrainAs(scoreRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }, text = viewModel.score.value ?: "",
                style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
