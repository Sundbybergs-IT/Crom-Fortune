package com.sundbybergsit.cromfortune.ui.settings

import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.LeafScreen
import com.sundbybergsit.cromfortune.OverflowMenu
import com.sundbybergsit.cromfortune.R

@Composable
fun Settings(viewModel: SettingsViewModel, onBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val context = LocalContext.current
    val showStockRetrievalTimeIntervalsDialog = remember { mutableStateOf(false) }
    if (showStockRetrievalTimeIntervalsDialog.value) {
        AndroidView(factory = { context ->
            val dialog = TimeIntervalStockRetrievalDialogFragment()
            dialog.onCreateView(
                LayoutInflater.from(context),
                null,
                null
            )!!
        })
    }
    val showSupportedStocksDialog = remember { mutableStateOf(false) }
    if (showSupportedStocksDialog.value) {
        AndroidView(factory = { context ->
            val dialog = SupportedStockDialogFragment()
            dialog.onCreateView(
                LayoutInflater.from(context),
                null,
                null
            ) ?: View(context)
        })
    }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.settings_title), style = MaterialTheme.typography.h6)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            }, actions = {
                OverflowMenu(
                    onNavigateTo = onNavigateTo, contentDescription = "Settings Menu",
                    route = LeafScreen.BottomSheetsSettings.route
                )
            })
    }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
        }
    }
}
