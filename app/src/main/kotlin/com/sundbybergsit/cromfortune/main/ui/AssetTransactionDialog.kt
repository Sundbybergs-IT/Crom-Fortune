package com.sundbybergsit.cromfortune.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.TradableAsset
import com.sundbybergsit.cromfortune.domain.TransactionAction
import com.sundbybergsit.cromfortune.main.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ASSET_TRANSACTION_DATE_FORMAT = "MM/dd/yyyy"

internal fun buildAssetTransaction(
    asset: TradableAsset,
    action: TransactionAction,
    dateInMillis: Long,
    quantityText: String,
    unitPriceText: String,
    commissionFeeText: String
): AssetTransaction {
    val quantity = quantityText.toBigDecimalOrNull() ?: throw IllegalArgumentException("Invalid quantity")
    require(quantity.scale().coerceAtLeast(0) <= asset.quantityScale) {
        "${asset.symbol} supports at most ${asset.quantityScale} decimal places"
    }
    return AssetTransaction(
        assetId = asset.id,
        assetType = asset.type,
        symbol = asset.symbol,
        displayName = asset.displayName,
        quoteCurrencyCode = asset.quoteCurrency.currencyCode,
        action = action,
        dateInMillis = dateInMillis,
        unitPrice = unitPriceText.toBigDecimalOrNull() ?: throw IllegalArgumentException("Invalid price"),
        commissionFee = commissionFeeText.ifBlank { "0" }.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Invalid commission fee"),
        quantity = quantity
    )
}

@Composable
fun RegisterAssetTransactionDialog(
    action: TransactionAction,
    initialAssetId: String? = null,
    onDismiss: () -> Unit,
    onSave: (AssetTransaction) -> Unit
) {
    var selectedType by remember(initialAssetId) {
        mutableStateOf(AssetCatalog.findById(initialAssetId.orEmpty())?.type ?: AssetType.STOCK)
    }
    var selectedAsset by remember(initialAssetId) {
        mutableStateOf(AssetCatalog.findById(initialAssetId.orEmpty()) ?: AssetCatalog.stocks.first())
    }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var assetMenuExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var commissionFee by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat(ASSET_TRANSACTION_DATE_FORMAT, Locale.getDefault()) }
    val initialDateInMillis = remember { Calendar.getInstance().timeInMillis }
    val transactionDate: MutableState<TextFieldValue> = remember {
        mutableStateOf(TextFieldValue(dateFormat.format(initialDateInMillis)))
    }
    val datePickerState: DatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateInMillis
    )
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val availableAssets = AssetCatalog.assets.filter { asset -> asset.type == selectedType }

    if (showDatePicker) {
        DateSelectionDialog(
            onDismiss = { showDatePicker = false },
            datePickerState = datePickerState,
            dateMutableState = transactionDate,
            simpleDateFormat = dateFormat
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            Text(
                stringResource(
                    if (action == TransactionAction.BUY) R.string.action_asset_buy
                    else R.string.action_asset_sell
                ),
                style = MaterialTheme.typography.titleSmall
            )
            TextButton(onClick = { typeMenuExpanded = true }) { Text("Type: ${selectedType.name}") }
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                AssetType.entries.forEach { type ->
                    DropdownMenuItem(text = { Text(type.name) }, onClick = {
                        selectedType = type
                        selectedAsset = AssetCatalog.assets.first { asset -> asset.type == type }
                        typeMenuExpanded = false
                    })
                }
            }
            TextButton(onClick = { assetMenuExpanded = true }) {
                Text("${selectedAsset.displayName} (${selectedAsset.symbol})")
            }
            DropdownMenu(expanded = assetMenuExpanded, onDismissRequest = { assetMenuExpanded = false }) {
                availableAssets.forEach { asset ->
                    DropdownMenuItem(text = { Text("${asset.displayName} (${asset.symbol})") }, onClick = {
                        selectedAsset = asset
                        assetMenuExpanded = false
                    })
                }
            }
            TextButton(onClick = { showDatePicker = true }) {
                Text(transactionDate.value.text)
            }
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text(stringResource(R.string.home_add_stock_quantity_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text(stringResource(R.string.generic_price_per_stock)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = commissionFee,
                onValueChange = { commissionFee = it },
                label = { Text(stringResource(R.string.home_add_commission_fee_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                supportingText = error?.let { message -> ({ Text(message) }) },
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
                TextButton(onClick = {
                    try {
                        val dateInMillis = checkNotNull(dateFormat.parse(transactionDate.value.text)).time
                        onSave(buildAssetTransaction(selectedAsset, action, dateInMillis, quantity, unitPrice, commissionFee))
                        onDismiss()
                    } catch (exception: RuntimeException) {
                        error = exception.message ?: "Invalid transaction"
                    }
                }) { Text(stringResource(android.R.string.ok)) }
            }
        }
    }
}
