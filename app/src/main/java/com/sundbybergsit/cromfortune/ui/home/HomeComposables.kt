package com.sundbybergsit.cromfortune.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.pager.HorizontalPager
import com.sundbybergsit.cromfortune.R

@Composable
fun Home(viewModel: HomeViewModel) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.home_title), style = MaterialTheme.typography.h6)
            }
        )
    }) { paddingValues ->
        val modifier = Modifier.fillMaxSize()
        Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
            // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
            HorizontalPager(modifier = modifier, count = 2) { page ->
                if (page == 0) {
                    // FIXME: Implement YOUR stocks, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                    StockOrderAggregates(
                        modifier = modifier,
                        title = stringResource(id = R.string.home_stocks_personal_title),
                        fabActive = true,
                        viewModel = viewModel
                    )
                } else {
                    // FIXME: Implement Croms stocks, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                    StockOrderAggregates(
                        modifier = modifier,
                        title = stringResource(id = R.string.home_stocks_crom_title),
                        fabActive = false,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun StockOrderAggregates(modifier: Modifier, title: String, fabActive: Boolean, viewModel: HomeViewModel) {
    ConstraintLayout(modifier = modifier) {
        val (titleRef, fabRef) = createRefs()
        Text(modifier = Modifier.constrainAs(titleRef) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, text = title)
        if (fabActive) {
            val (showDialog, setShowDialog) = remember { mutableStateOf(false) }
            RegisterBuyStockDialogFragment(showDialog = showDialog, setShowDialog = setShowDialog)
            FloatingActionButton(modifier = Modifier.constrainAs(fabRef) {
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }.padding(16.dp), onClick = { setShowDialog(true) }) {
            }
        }
    }
}

@Composable
fun RegisterBuyStockDialogFragment(showDialog: Boolean, setShowDialog: (Boolean) -> Unit) {
    // FIXME: Convert RegisterBuyStockDialogFragment into a composable
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
            },
            title = {
                Text(stringResource(id = R.string.action_stock_buy))
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Change the state to close the dialog
                        setShowDialog(false)
                    },
                ) {
                    Text(stringResource(id = R.string.action_ok))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Change the state to close the dialog
                        setShowDialog(false)
                    },
                ) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            text = {
                Text("This is a text on the dialog")
            },
        )
    }
}

