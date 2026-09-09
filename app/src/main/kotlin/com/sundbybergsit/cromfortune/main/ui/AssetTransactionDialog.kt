package com.sundbybergsit.cromfortune.main.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.TradableAsset
import com.sundbybergsit.cromfortune.domain.TransactionAction
import com.sundbybergsit.cromfortune.main.R
import java.math.BigDecimal
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
    var saveError by remember { mutableStateOf<String?>(null) }
    val availableAssets = AssetCatalog.assets.filter { asset -> asset.type == selectedType }
    val parsedQuantity = quantity.toBigDecimalOrNull()
    val quantityIsValid = parsedQuantity != null && parsedQuantity > BigDecimal.ZERO &&
        parsedQuantity.scale().coerceAtLeast(0) <= selectedAsset.quantityScale
    val parsedUnitPrice = unitPrice.toBigDecimalOrNull()
    val unitPriceIsValid = parsedUnitPrice != null && parsedUnitPrice > BigDecimal.ZERO
    val parsedCommission = commissionFee.ifBlank { "0" }.toBigDecimalOrNull()
    val commissionIsValid = parsedCommission != null && parsedCommission >= BigDecimal.ZERO
    val formIsValid = quantityIsValid && unitPriceIsValid && commissionIsValid

    if (showDatePicker) {
        DateSelectionDialog(
            onDismiss = { showDatePicker = false },
            datePickerState = datePickerState,
            dateMutableState = transactionDate,
            simpleDateFormat = dateFormat
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (action == TransactionAction.BUY) R.string.asset_transaction_title_buy else R.string.asset_transaction_title_sell))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.asset_transaction_asset_type),
                    style = MaterialTheme.typography.labelMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssetType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                if (selectedType != type) {
                                    selectedType = type
                                    selectedAsset = AssetCatalog.assets.first { it.type == type }
                                }
                            },
                            label = { Text(assetTypeName(type)) }
                        )
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "${selectedAsset.displayName} (${selectedAsset.symbol})", onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.asset_transaction_asset)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(Modifier.matchParentSize().clickable { assetMenuExpanded = true })
                    DropdownMenu(assetMenuExpanded, { assetMenuExpanded = false }) {
                        availableAssets.forEach { asset ->
                            DropdownMenuItem(text = { Text("${asset.displayName} (${asset.symbol})") }, onClick = {
                                selectedAsset = asset
                                assetMenuExpanded = false
                            })
                        }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = transactionDate.value.text, onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.generic_date)) },
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(Modifier.matchParentSize().clickable { showDatePicker = true })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it; saveError = null }, singleLine = true,
                        label = { Text(stringResource(R.string.home_add_stock_quantity_label)) },
                        isError = quantity.isNotEmpty() && !quantityIsValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitPrice, onValueChange = { unitPrice = it; saveError = null }, singleLine = true,
                        label = { Text(stringResource(R.string.asset_transaction_price)) },
                        suffix = { Text(selectedAsset.quoteCurrency.currencyCode) },
                        isError = unitPrice.isNotEmpty() && !unitPriceIsValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = commissionFee, onValueChange = { commissionFee = it; saveError = null }, singleLine = true,
                    label = { Text(stringResource(R.string.generic_commission_fee)) }, suffix = { Text("SEK") },
                    isError = commissionFee.isNotEmpty() && !commissionIsValid,
                    supportingText = saveError?.let { message -> ({ Text(message) }) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
        confirmButton = {
            TextButton(enabled = formIsValid, onClick = {
                try {
                    val dateInMillis = checkNotNull(dateFormat.parse(transactionDate.value.text)).time
                    onSave(buildAssetTransaction(selectedAsset, action, dateInMillis, quantity, unitPrice, commissionFee))
                    onDismiss()
                } catch (exception: RuntimeException) {
                    saveError = exception.message ?: "Invalid transaction"
                }
            }) {
                Text(stringResource(if (action == TransactionAction.BUY) R.string.asset_transaction_confirm_buy else R.string.asset_transaction_confirm_sell))
            }
        }
    )
}

@Composable
private fun assetTypeName(type: AssetType): String = stringResource(
    if (type == AssetType.STOCK) R.string.asset_type_stock else R.string.asset_type_crypto
)
