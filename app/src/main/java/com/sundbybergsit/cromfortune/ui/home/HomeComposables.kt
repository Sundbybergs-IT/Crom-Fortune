package com.sundbybergsit.cromfortune.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.pager.HorizontalPager
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.contentDescription
import com.sundbybergsit.cromfortune.ui.DialogButton
import java.time.Instant

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
            val showDialog = remember { mutableStateOf(false) }
            RegisterBuyStockAlertDialog(
                showDialog = showDialog.value,
                // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                onConfirm = {},
                onDismiss = { showDialog.value = false })
            FloatingActionButton(modifier = Modifier
                .constrainAs(fabRef) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .padding(16.dp), onClick = { showDialog.value = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Floating Action Button Icon"
                )
            }
        }
    }
}

@Composable
fun RegisterBuyStockAlertDialog(showDialog: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    // FIXME: Convert RegisterBuyStockDialogFragment into a composable
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            ConstraintLayout {
                val (titleRef, dateRef, currencyRef, amountRef, nameRef, priceRef, courtageRef, buttonsRef) = createRefs()
                Text(modifier = Modifier.constrainAs(titleRef) {
                    top.linkTo(parent.top)
                }, text = stringResource(id = R.string.action_stock_buy))
                val dateMutableState: MutableState<TextFieldValue> = remember {
                    mutableStateOf(
                        TextFieldValue(text = Instant.ofEpochMilli(System.currentTimeMillis()).toString())
                    )
                }
                val dateErrorMessageMutableState: MutableState<String> = remember { mutableStateOf("") }
                val dateErrorMutableState: MutableState<Boolean> = remember { mutableStateOf(false) }
                InputValidatedOutlinedTextField(
                    modifier = Modifier
                        .constrainAs(dateRef) {
                            top.linkTo(titleRef.bottom)
                            start.linkTo(parent.start)
                        }
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxWidth(),
                    value = dateMutableState,
                    isError = dateErrorMutableState.value,
                    errorMessage = dateErrorMessageMutableState.value,
                    contentDescriptor = "Date Input Text"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .constrainAs(currencyRef) {
                            top.linkTo(titleRef.bottom)
                            start.linkTo(dateRef.start)
                        }
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxWidth(),
                    onValueChange = {},
                    enabled = false,
                    value = ""
                )
                val amountMutableState: MutableState<TextFieldValue> =
                    remember { mutableStateOf(TextFieldValue(text = "")) }
                val amountErrorMessageMutableState: MutableState<String> = remember { mutableStateOf("") }
                val amountErrorMutableState: MutableState<Boolean> = remember { mutableStateOf(false) }
                InputValidatedOutlinedTextField(
                    modifier = Modifier
                        .constrainAs(amountRef) {
                            top.linkTo(dateRef.bottom)
                            start.linkTo(parent.start)
                        }
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxWidth(),
                    value = amountMutableState,
                    isError = amountErrorMutableState.value,
                    errorMessage = amountErrorMessageMutableState.value,
                    contentDescriptor = "Amount Input Text"
                )
                // FIXME: Finish dialog, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                Row(modifier = Modifier
                    .padding(all = 16.dp)
                    .constrainAs(buttonsRef) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }) {
                    DialogButton(text = stringResource(id = R.string.action_cancel), onClick = onDismiss)
                    DialogButton(text = stringResource(id = android.R.string.ok), onClick = onConfirm)
                }
            }
        }
    }
}

@Composable
private fun InputValidatedOutlinedTextField(
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    value: MutableState<TextFieldValue>,
    label: @Composable() (() -> Unit)? = null,
    contentDescriptor: String,
    errorMessage: String,
    isError: Boolean,
    horizontalPadding: Dp = 28.dp
) {
    Column(
        modifier = modifier.padding(horizontal = horizontalPadding)
    ) {
        val shouldInputBeHighlighted = remember { mutableStateOf(false) }
        Box(
            Modifier
                .onFocusChanged {
                    shouldInputBeHighlighted.value = it.hasFocus
                }
                .background(
                    color = if (shouldInputBeHighlighted.value && !isError) {
                        MaterialTheme.colors.primarySurface
                    } else {
                        MaterialTheme.colors.background
                    }, shape = MaterialTheme.shapes.small
                )
                .padding(4.dp)
                .clip(MaterialTheme.shapes.small)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth()
                    .contentDescription(contentDescriptor),
                singleLine = true,
                value = value.value,
                isError = isError,
                onValueChange = { newText: TextFieldValue ->
                    value.value = newText
                },
                textStyle = MaterialTheme.typography.body1,
                label = label,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedBorderColor = MaterialTheme.colors.onSurface),
                trailingIcon = if (isError) {
                    {
                        Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
                    }
                } else {
                    null
                }
            )
        }
        if (isError) {
            Text(
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                text = errorMessage,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error
            )
        }
    }
}

