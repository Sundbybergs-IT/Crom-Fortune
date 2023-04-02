package com.sundbybergsit.cromfortune.ui.settings

import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.R

@Composable
fun Settings(viewModel: SettingsViewModel, onBack: () -> Unit) {
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
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = {
                    showMenu = !showMenu
                    if (showMenu) {
                        // Show options
//                        onNavigateTo.invoke(route)
                        /*
                        // FIXME: Move to bottomsheet?, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                        TextButton(
                            onClick = {
                                showStockRetrievalTimeIntervalsDialog.value = true
                            }
                        ) {
                            Text(
                                text = stringResource(id = R.string.action_configure_stock_retrieval_intervals)
                            )
                        }
                        TextButton(
                            onClick = {
                                showSupportedStocksDialog.value = true
                            }
                        ) {
                            ButtonText(
                                text = stringResource(id = R.string.action_stocks_supported)
                            )
                        }
                        TextButton(
                            onClick = {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/Sundbybergs-IT/Crom-Fortune/issues")
                                )
                                context.startActivity(browserIntent)
                            }
                        ) {
                            ButtonText(
                                text = stringResource(id = R.string.generic_to_do)
                            )
                        }
                         */
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        tint = MaterialTheme.colors.surface,
                        contentDescription = "Overflow menu",
                    )
                }
            }
        )
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
