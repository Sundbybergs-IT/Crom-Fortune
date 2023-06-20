package com.sundbybergsit.cromfortune.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.contentDescription
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

private const val DATE_FORMAT = "MM/dd/yyyy"

@Composable
fun RegisterSellStockAlertDialog(showDialog: Boolean, onDismiss: () -> Unit, onSave: (StockOrder) -> Unit) {
    // FIXME: Implement, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
    if (showDialog) {

    }
}

@Composable
fun RegisterSplitStockAlertDialog(showDialog: Boolean, onDismiss: () -> Unit, onSave: (StockSplit) -> Unit) {
    // FIXME: Implement, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
    if (showDialog) {

    }
}

@Composable
fun RegisterBuyStockAlertDialog(
    showDialog: Boolean, onDismiss: () -> Unit, onSave: (StockOrder) -> Unit
) {
    // FIXME: Convert RegisterBuyStockDialogFragment into a composable
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            val scrollState = rememberScrollState()
            ConstraintLayout(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .verticalScroll(state = scrollState)
            ) {
                val (titleRef, dateRef, stockQuantityRef, stockNameRef, stockPriceRef, stockCurrencyRef, commissionFeeRef, buttonsRef) = createRefs()

                Text(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .constrainAs(titleRef) {
                            top.linkTo(parent.top)
                        }, text = stringResource(id = R.string.action_stock_buy)
                )
                val myCalendar = Calendar.getInstance()
                val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                val dateMutableState: MutableState<TextFieldValue> = remember {
                    mutableStateOf(
                        TextFieldValue(text = sdf.format(myCalendar.time))
                    )
                }
                val dateErrorMutableState: MutableState<Boolean> = remember { mutableStateOf(false) }
                val dateErrorMessageMutableState: MutableState<String> = remember { mutableStateOf("") }
                InputValidatedOutlinedTextField(modifier = Modifier
                    .constrainAs(dateRef) {
                        top.linkTo(titleRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.home_add_stock_date_label)) },
                    value = dateMutableState,
                    isError = dateErrorMutableState.value,
                    errorMessage = dateErrorMessageMutableState.value,
                    contentDescriptor = "Date Input Text"
                )
                val stockQuantityMutableState: MutableState<TextFieldValue> =
                    remember { mutableStateOf(TextFieldValue(text = "")) }
                val stockQuantityErrorMutableState: MutableState<Boolean> = remember { mutableStateOf(false) }
                val stockQuantityErrorMessageMutableState: MutableState<String> = remember { mutableStateOf("") }
                InputValidatedOutlinedTextField(modifier = Modifier
                    .constrainAs(stockQuantityRef) {
                        top.linkTo(dateRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.home_add_stock_quantity_label)) },
                    value = stockQuantityMutableState,
                    isError = stockQuantityErrorMutableState.value,
                    errorMessage = stockQuantityErrorMessageMutableState.value,
                    contentDescriptor = "Stock Quantity Input Text"
                )
                val stockNameMutableState: MutableState<TextFieldValue> =
                    remember { mutableStateOf(TextFieldValue(text = "")) }
                val stockNameErrorMutableState: MutableState<Boolean> = remember { mutableStateOf(false) }
                val stockNameErrorMessageMutableState: MutableState<String> = remember { mutableStateOf("") }
                val currencyMutableState: MutableState<TextFieldValue> = remember {
                    mutableStateOf(
                        TextFieldValue(text = "")
                    )
                }
                // FIXME: Auto-complete for stock names
                InputValidatedOutlinedTextField(modifier = Modifier
                    .constrainAs(stockNameRef) {
                        top.linkTo(stockQuantityRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.home_add_stock_name_label)) },
                    value = stockNameMutableState,
                    isError = stockNameErrorMutableState.value,
                    errorMessage = stockNameErrorMessageMutableState.value,
                    contentDescriptor = "Stock Name Input Text",
                    onFocusChanged = {
                        if (!it.hasFocus) {
                            val find =
                                StockPrice.SYMBOLS.find { triple -> "${triple.second} (${triple.first})" == stockNameMutableState.value.text }
                            if (find != null) {
                                currencyMutableState.value = TextFieldValue(find.third)
                            }
                        }
                    })
                val priceMutableState: MutableState<TextFieldValue> =
                    remember { mutableStateOf(TextFieldValue(text = "")) }
                val priceErrorMutableState: MutableState<Boolean> = remember { mutableStateOf(false) }
                val priceErrorMessageMutableState: MutableState<String> = remember { mutableStateOf("") }
                InputValidatedOutlinedTextField(modifier = Modifier
                    .constrainAs(stockPriceRef) {
                        top.linkTo(stockNameRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.generic_price_per_stock)) },
                    value = priceMutableState,
                    isError = priceErrorMutableState.value,
                    errorMessage = priceErrorMessageMutableState.value,
                    contentDescriptor = "Stock Price Input Text"
                )
                // FIXME: Fill in currency automatically based on chosen stock
                OutlinedTextField(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .constrainAs(stockCurrencyRef) {
                            top.linkTo(stockPriceRef.bottom)
                            start.linkTo(parent.start)
                        }
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.home_add_stock_currency_label)) },
                    onValueChange = {},
                    enabled = false,
                    value = currencyMutableState.value,
                    textStyle = MaterialTheme.typography.body1,
                )
                val commissionFeeMutableState: MutableState<TextFieldValue> =
                    remember { mutableStateOf(TextFieldValue(text = "")) }
                val commissionFeeErrorMutableState: MutableState<Boolean> = remember { mutableStateOf(false) }
                val commissionFeeErrorMessageMutableState: MutableState<String> = remember { mutableStateOf("") }
                InputValidatedOutlinedTextField(modifier = Modifier
                    .constrainAs(commissionFeeRef) {
                        top.linkTo(stockCurrencyRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.home_add_commission_fee_label)) },
                    value = commissionFeeMutableState,
                    isError = commissionFeeErrorMutableState.value,
                    errorMessage = commissionFeeErrorMessageMutableState.value,
                    contentDescriptor = "Commission Fee Input Text"
                )
                Row(modifier = Modifier
                    .padding(all = 16.dp)
                    .constrainAs(buttonsRef) {
                        top.linkTo(commissionFeeRef.bottom)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }) {
                    DialogButton(text = stringResource(id = R.string.action_cancel), onClick = onDismiss)
                    val context = LocalContext.current
                    DialogButton(text = stringResource(id = android.R.string.ok), onClick = {
                        try {
                            dateMutableState.value.validateDate(
                                context = context,
                                errorMutableState = dateErrorMutableState,
                                errorMessageMutableState = dateErrorMessageMutableState,
                                pattern = DATE_FORMAT
                            )
                            stockQuantityMutableState.value.validateInt(
                                context = context,
                                errorMutableState = stockQuantityErrorMutableState,
                                errorMessageMutableState = stockQuantityErrorMessageMutableState
                            )
                            stockQuantityMutableState.value.validateMinQuantity(
                                context = context,
                                errorMutableState = stockQuantityErrorMutableState,
                                errorMessageMutableState = stockQuantityErrorMessageMutableState,
                                minValue = 1
                            )
                            stockNameMutableState.value.validateStockName(
                                context = context,
                                errorMutableState = stockNameErrorMutableState,
                                errorMessageMutableState = stockNameErrorMessageMutableState
                            )
                            val stockSymbol =
                                stockNameMutableState.value.text.substringAfterLast('(').substringBeforeLast(')')
                            priceMutableState.value.validateDouble(
                                context = context,
                                errorMutableState = priceErrorMutableState,
                                errorMessageMutableState = priceErrorMessageMutableState
                            )
                            commissionFeeMutableState.value.validateDouble(
                                context = context,
                                errorMutableState = commissionFeeErrorMutableState,
                                errorMessageMutableState = commissionFeeErrorMessageMutableState
                            )
                            val dateAsString = dateMutableState.value.text
                            val inputDate = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateAsString)

                            val currency = Currency.getInstance(currencyMutableState.value.text)
                            // TODO: Convert commission fee (in SEK) to selected currency
                            val stockOrder = StockOrder(
                                "Buy",
                                currency.toString(),
                                inputDate.time,
                                stockSymbol,
                                priceMutableState.value.text.toDouble(),
                                commissionFeeMutableState.value.text.toDouble(),
                                stockQuantityMutableState.value.text.toInt()
                            )
                            onSave.invoke(stockOrder)
                            onDismiss.invoke()
                        } catch (e: ValidatorException) {
                            // Shit happens ...
                        }
                    })
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
    label: @Composable (() -> Unit)? = null,
    contentDescriptor: String,
    errorMessage: String,
    isError: Boolean,
    horizontalPadding: Dp = 28.dp,
    onFocusChanged: ((FocusState) -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(horizontal = horizontalPadding)
    ) {
        val shouldInputBeHighlighted = remember { mutableStateOf(false) }
        Box(
            Modifier
                .onFocusChanged {
                    shouldInputBeHighlighted.value = it.hasFocus
                    onFocusChanged?.invoke(it)
                }
                .background(
                    color = if (shouldInputBeHighlighted.value && !isError) {
                        MaterialTheme.colors.primarySurface
                    } else {
                        MaterialTheme.colors.background
                    }, shape = MaterialTheme.shapes.small
                )
                .padding(4.dp)
                .clip(MaterialTheme.shapes.small)) {
            OutlinedTextField(modifier = Modifier
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
                })
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
