package com.sundbybergsit.cromfortune.main

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@Composable
internal fun RefreshFromViewStateLaunchedEffect(
    viewState: StockPriceRepository.ViewState?,
    viewModel: DashboardViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = viewState) {
        when (viewState) {
            is StockPriceRepository.ViewState -> {
                viewModel.refresh(
                    context = context,
                    timestamp = viewState.instant,
                    stockPrices = viewState.stockPrices
                )
            }

            else -> {
                // Do nothing
            }
        }
    }
}

@Composable
internal fun AnimateRotationLaunchedEffect(
    rotation: Animatable<Float, AnimationVector1D>,
    currentRotationMutableState: MutableState<Float>,
    durationInMs: Int,
) {
    LaunchedEffect(key1 = Unit) {
        rotation.animateTo(
            targetValue = currentRotationMutableState.value + 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationInMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ) {
            currentRotationMutableState.value = value
        }
    }
}

@Composable
internal fun LoadValueFromParameterLaunchedEffect(
    stockSymbol: String?,
    stockNameMutableState: MutableState<TextFieldValue>,
    stockCurrencyMutableState: MutableState<TextFieldValue>,
) {
    LaunchedEffect(stockSymbol) {
        val triple = StockPrice.SYMBOLS.find { triple -> triple.first == stockSymbol }
        triple?.let { nullSafeTriple ->
            stockNameMutableState.value = TextFieldValue("${nullSafeTriple.second} (${nullSafeTriple.first})")
            stockCurrencyMutableState.value = TextFieldValue(nullSafeTriple.third)
        }
    }
}

@Composable
internal fun UpdateTimePickerLaunchedEffect(
    settingsViewState: StockRetrievalSettings.ViewState,
    dialogViewState: DialogHandler.DialogViewState.ShowStockRetrievalTimeIntervalsDialog,
    selectedDaysMutableState: MutableState<Set<DayOfWeek>>,
    fromTimePickerState: MutableState<TimePickerState?>,
    toTimePickerState: MutableState<TimePickerState?>
) {
    DisposableEffect(key1 = settingsViewState) {
        val settingsViewState = dialogViewState.stockRetrievalSettings.timeInterval
        selectedDaysMutableState.value = settingsViewState.value.weekDays.toSet()
        fromTimePickerState.value = TimePickerState(
            initialHour = settingsViewState.value.fromTimeHours,
            initialMinute = settingsViewState.value.fromTimeMinutes,
            is24Hour = true
        )
        toTimePickerState.value = TimePickerState(
            initialHour = settingsViewState.value.toTimeHours,
            initialMinute = settingsViewState.value.toTimeMinutes,
            is24Hour = true
        )
        onDispose {
            fromTimePickerState.value = null
            toTimePickerState.value = null
        }
    }
}

@Composable
internal fun PagerStateSelectionHapticFeedbackLaunchedEffect(
    pagerState: PagerState,
    view: View,
    changedState: State<Boolean>
) {
    LaunchedEffect(key1 = pagerState.currentPage) {
        if (changedState.value) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }
}

@Composable
internal fun ShowSnackbarLaunchedEffect(
    dialogHandler: DialogHandler,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    view: View
) {
    LaunchedEffect(key1 = "ShowSnackbarLaunchedEffect") {
        coroutineScope.launch {
            dialogHandler.snackbarFlow
                .collect { snackbar ->
                    val message = snackbar?.first
                    val actionPair = snackbar?.second
                    if (message?.isNotEmpty() == true) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        dialogHandler.acknowledgeSnack()
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(message = message, actionLabel = actionPair?.first)
                            .let { result ->
                                if (result == SnackbarResult.ActionPerformed) {
                                    actionPair?.second?.invoke()
                                }
                            }
                    }
                }
        }
    }
}

