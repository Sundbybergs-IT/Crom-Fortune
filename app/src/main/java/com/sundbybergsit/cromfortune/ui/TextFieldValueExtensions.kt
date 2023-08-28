package com.sundbybergsit.cromfortune.ui

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

fun TextFieldValue.validateStockQuantity(
    context: Context,
    errorMutableState: MutableState<Boolean>,
    errorMessageMutableState: MutableState<String>,
    stockName: String,
    homeViewModel : HomeViewModel
) {
    when {
        !homeViewModel.hasNumberOfStocks(context= context, stockName, quantity = text.toInt()) -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.home_remove_stock_quantity_error_insufficient)
            throw ValidatorException()
        }
        else -> {
            errorMutableState.value = false
            errorMessageMutableState.value = ""
        }
    }
}

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
    when {
        text.isEmpty() -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_empty)
            throw ValidatorException()
        }
        SimpleDateFormat(pattern, Locale.getDefault()).parse(text) == null -> {
            errorMutableState.value = true
            errorMessageMutableState.value = context.getString(R.string.generic_error_invalid_date)
            throw ValidatorException()
        }
        else -> {
            errorMutableState.value = false
            errorMessageMutableState.value = ""
        }
    }
}

fun TextFieldValue.validateDouble(
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
        text.toDoubleOrNull() == null -> {
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
        !StockPrice.SYMBOLS.map { pair -> "${pair.second} (${pair.first})" }
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
