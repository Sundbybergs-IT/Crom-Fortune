package com.sundbybergsit.cromfortune.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository

@Composable
fun Dashboard(viewModel: DashboardViewModel) {
    val viewState: StockPriceRepository.ViewState? by StockPriceRepository.stockPrices.observeAsState()
    val context = LocalContext.current
    LaunchedEffect(key1 = "RetrieveStocksLaunchedEffect") {
        when (viewState) {
            is StockPriceRepository.ViewState.VALUES -> {
                viewModel.refresh(
                    context = context,
                    (viewState as StockPriceRepository.ViewState.VALUES).instant,
                    (viewState as StockPriceRepository.ViewState.VALUES).stockPrices
                )
            }
            else -> {
                // Do nothing
            }
        }
    }
    Scaffold(topBar = {
        // FIXME: Implement action bar with back button, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
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
            Text(modifier = Modifier.constrainAs(scoreRef) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, text = viewModel.score.value ?: "")
        }
    }
}
