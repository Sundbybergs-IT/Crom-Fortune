package com.sundbybergsit.cromfortune

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshotFlow
import com.sundbybergsit.cromfortune.settings.StockRetrievalSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@Composable
internal fun PagerStateChangeDetectionLaunchedEffect(
    pagerState: PagerState,
    changedPagerMutableState: MutableState<Boolean>
) {
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { changedPagerMutableState.value = true }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && changedState.value) {
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
            dialogHandler
                .snackbarFlow
                .collect { snackbar ->
                    if (!snackbar.isNullOrEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                        dialogHandler.acknowledgeSnack()
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(snackbar)
                    }
                }
        }
    }
}

