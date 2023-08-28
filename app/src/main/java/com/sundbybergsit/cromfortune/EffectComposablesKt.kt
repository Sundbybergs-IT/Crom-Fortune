package com.sundbybergsit.cromfortune

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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

