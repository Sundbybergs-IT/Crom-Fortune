package com.sundbybergsit.cromfortune.main.ui.dashboard

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.main.AnimateRotationLaunchedEffect
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.RefreshFromViewStateLaunchedEffect
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.theme.Loss
import com.sundbybergsit.cromfortune.main.theme.Profit

@Composable
fun Dashboard(viewModel: DashboardViewModel, onBack: () -> Unit) {
    val viewState: StockPriceRepository.ViewState by StockPriceRepository.stockPricesStateFlow.collectAsState()
    RefreshFromViewStateLaunchedEffect(viewState = viewState, viewModel = viewModel)
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Transition")
    val currentRotationMutableState = remember { mutableFloatStateOf(0f) }
    val currentRotation by currentRotationMutableState
    val rotation = remember { Animatable(currentRotation) }
    val durationInMs = 6000
    val scoreState = viewModel.scoreStateFlow.collectAsState()
    AnimateRotationLaunchedEffect(
        rotation = rotation,
        currentRotationMutableState = currentRotationMutableState,
        durationInMs = durationInMs
    )
    val color: Color by animateColorAsState(
        targetValue =
        if (currentRotation > 0f && currentRotation <= 130f) {
            val toLoss by infiniteTransition.animateColor(
                initialValue = MaterialTheme.colorScheme.onSurfaceVariant,
                targetValue = Loss,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationInMs / 2, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = "To Loss"
            )
            toLoss
        } else if (currentRotation > 180f) {
            val fromProfit by infiniteTransition.animateColor(
                initialValue = MaterialTheme.colorScheme.onSurfaceVariant,
                targetValue = Profit,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationInMs / 2, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = "To Profit"
            )
            fromProfit
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }, label = "Color Transition"
    )
    val stonkTopMargin: Dp by animateDpAsState(
        targetValue =
        when {
            currentRotation > 60f && currentRotation <= 170f -> 400.dp
            currentRotation > 170f && currentRotation <= 200f -> 300.dp
            currentRotation > 200f && currentRotation <= 250f -> 200.dp
            currentRotation > 250f && currentRotation < 270f -> 100.dp
            else -> 0.dp
        },
        label = "Stonk Top Margin"
    )
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
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            }
        )
    }) { paddingValues ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val (imageRef, iconRef, scoreRef) = createRefs()
            createVerticalChain(imageRef, iconRef, scoreRef, chainStyle = ChainStyle.Packed)
            Image(
                modifier = Modifier
                    .constrainAs(imageRef) {
                        start.linkTo(parent.start, margin = (-200).dp + stonkTopMargin)
                        end.linkTo(parent.end)
                    }
                    .size(160.dp),
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.stonks),
                contentDescription = "Stonk"
            )
            Icon(
                modifier = Modifier
                    .constrainAs(iconRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .rotate(rotation.value)
                    .size(128.dp), imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                tint = color, contentDescription = "Profit or Loss"
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
        }
    }
}
