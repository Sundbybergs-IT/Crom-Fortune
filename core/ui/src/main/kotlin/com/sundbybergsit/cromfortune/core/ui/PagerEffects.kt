package com.sundbybergsit.cromfortune.core.ui

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State

@Composable
fun PagerStateSelectionHapticFeedbackLaunchedEffect(
    pagerState: PagerState,
    view: View,
    changedState: State<Boolean>
) {
    LaunchedEffect(pagerState.currentPage) {
        if (changedState.value) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }
}
