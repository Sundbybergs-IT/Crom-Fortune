package com.sundbybergsit.cromfortune.main.ui

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.main.R
import java.text.SimpleDateFormat
import java.util.Locale

fun TextFieldValue.validateMinQuantity(
    context: Context,
    errorMutableState: MutableState<Boolean>,
    errorMessageMutableState: MutableState<String>,
    minValue: Int
) {
    when {
        text.toInt() < minValue -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_invalid_quantity)
            throw ValidatorException()
        }

        else -> {
            errorMutableState.value = false
            errorMessageMutableState.value = ""
        }
    }
}

fun TextFieldValue.validateInt(
    context: Context,
    errorMutableState: MutableState<Boolean>,
    errorMessageMutableState: MutableState<String>
) {
    when {
        text.isEmpty() -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_empty)
            throw ValidatorException()
        }

        text.toIntOrNull() == null -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_invalid_number)
            throw ValidatorException()
        }

        else -> {
            errorMutableState.value = false
            errorMessageMutableState.value = ""
        }
    }
}

fun TextFieldValue.validateDate(
    context: Context,
    errorMutableState: MutableState<Boolean>,
    errorMessageMutableState: MutableState<String>, pattern: String
) {
    if (text.isEmpty()) {
        errorMutableState.value = true
        errorMessageMutableState.value = context.getString(R.string.generic_error_empty)
        throw ValidatorException()
    }

    try {
        val date = SimpleDateFormat(pattern, Locale.getDefault()).parse(text)
        if (date == null) {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_invalid_date)
            throw IllegalStateException()
        }
    } catch (e: Exception) {
        errorMutableState.value = true
        errorMessageMutableState.value = context.getString(R.string.generic_error_invalid_date)
        throw ValidatorException()
    }

    errorMutableState.value = false
    errorMessageMutableState.value = ""
}

fun TextFieldValue.validateStockName(
    context: Context,
    errorMutableState: MutableState<Boolean>,
    errorMessageMutableState: MutableState<String>
) {
    when {
        text.isEmpty() -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_empty)
            throw ValidatorException()
        }

        !AssetCatalog.stocks.map { asset -> "${asset.displayName} (${asset.symbol})" }
            .toMutableList().contains(text) -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_invalid_stock_symbol)
            throw ValidatorException()
        }

        else -> {
            errorMutableState.value = false
            errorMessageMutableState.value = ""
        }
    }
}
