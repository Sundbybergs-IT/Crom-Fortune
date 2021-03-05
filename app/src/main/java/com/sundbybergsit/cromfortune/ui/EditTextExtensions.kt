package com.sundbybergsit.cromfortune.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

fun EditText.transformIntoDatePicker(context: Context, format: String, maxDate: Date? = null, textInputLayout: TextInputLayout) {

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
