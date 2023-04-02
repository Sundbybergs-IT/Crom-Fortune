package com.sundbybergsit.cromfortune

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                .collect { snackbarData ->
                    if (snackbarData != null && snackbarData.isNotEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                        dialogHandler.acknowledgeSnack()
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(snackbarData)
                    }
                }
        }
    }
}
