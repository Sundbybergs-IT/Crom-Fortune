package com.sundbybergsit.cromfortune.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.StockPrice
import java.text.SimpleDateFormat
import java.util.*

fun EditText.transformIntoDatePicker(
    context: Context,
    format: String,
    maxDate: Date? = null,
    textInputLayout: TextInputLayout
) {

    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    val myCalendar = Calendar.getInstance()
    val datePickerOnDataSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            setText(sdf.format(myCalendar.time))
            textInputLayout.error = null
        }

    setOnClickListener {
        DatePickerDialog(
            context, datePickerOnDataSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        ).run {
            maxDate?.time?.also { datePicker.maxDate = it }
            show()
        }
    }

}

fun EditText.transformIntoTimePicker(context: Context, format: String, textInputLayout: TextInputLayout) {

    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    val myCalendar = Calendar.getInstance()
    val datePickerOnDataSetListener =
        TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)
            val sdf = SimpleDateFormat(format, Locale.ROOT)
            setText(sdf.format(myCalendar.time))
            textInputLayout.error = null
        }

    setOnClickListener {
        TimePickerDialog(
            context, datePickerOnDataSetListener, myCalendar
                .get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), true
        ).run {
            show()
        }
    }

}

fun EditText.validateMinQuantity(inputLayout: TextInputLayout, minValue: Int) {
    when {
        text.toString().toInt() < minValue -> {
            inputLayout.error = context.getString(R.string.generic_error_invalid_quantity)
            requestFocus()
            throw ValidatorException()
        }
        else -> {
            inputLayout.error = null
        }
    }
}

fun EditText.validateInt(inputLayout: TextInputLayout) {
    when {
        text.toString().isEmpty() -> {
            inputLayout.error = context.getString(R.string.generic_error_empty)
            requestFocus()
            throw ValidatorException()
        }
        text.toString().toIntOrNull() == null -> {
            inputLayout.error = context.getString(R.string.generic_error_invalid_number)
            requestFocus()
            throw ValidatorException()
        }
        else -> {
            inputLayout.error = null
        }
    }
}

fun EditText.validateDate(inputLayout: TextInputLayout, pattern: String) {
    when {
        text.toString().isEmpty() -> {
            inputLayout.error = context.getString(R.string.generic_error_empty)
            requestFocus()
            throw ValidatorException()
        }
        SimpleDateFormat(pattern, Locale.getDefault()).parse(text.toString()) == null -> {
            inputLayout.error = context.getString(R.string.generic_error_invalid_date)
            requestFocus()
            throw ValidatorException()
        }
        else -> {
            inputLayout.error = null
        }
    }
}

fun EditText.validateCurrency(inputLayout: TextInputLayout) {
    when {
        text.toString().isEmpty() -> {
            inputLayout.error = context.getString(R.string.generic_error_empty)
            requestFocus()
            throw ValidatorException()
        }
        !StockPrice.CURRENCIES.contains(text.toString()) -> {
            inputLayout.error = context.getString(R.string.generic_error_invalid_stock_symbol)
            requestFocus()
            throw ValidatorException()
        }
        else -> {
            inputLayout.error = null
        }
    }
}

fun EditText.validateDouble(inputLayout: TextInputLayout) {
    when {
        text.toString().isEmpty() -> {
            inputLayout.error = context.getString(R.string.generic_error_empty)
            requestFocus()
            throw ValidatorException()
        }
        text.toString().toDoubleOrNull() == null -> {
            inputLayout.error = context.getString(R.string.generic_error_invalid_number)
            requestFocus()
            throw ValidatorException()
        }
        else -> {
            inputLayout.error = null
        }
    }
}

fun EditText.validateStockName(inputLayout: TextInputLayout) {
    when {
        text.toString().isEmpty() -> {
            inputLayout.error = context.getString(R.string.generic_error_empty)
            requestFocus()
            throw ValidatorException()
        }
        !StockPrice.SYMBOLS.map { pair -> "${pair.second} (${pair.first})" }
            .toMutableList().contains(text.toString()) -> {
            inputLayout.error = context.getString(R.string.generic_error_invalid_stock_symbol)
            requestFocus()
            throw ValidatorException()
        }
        else -> {
            inputLayout.error = null
        }
    }
}

class ValidatorException : Exception()
