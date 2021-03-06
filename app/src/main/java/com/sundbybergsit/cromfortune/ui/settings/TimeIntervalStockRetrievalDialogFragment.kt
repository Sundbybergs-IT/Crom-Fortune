package com.sundbybergsit.cromfortune.ui.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.google.android.material.textfield.TextInputLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.transformIntoTimePicker
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.*

private const val TIME_FORMAT = "HH:mm"

class TimeIntervalStockRetrievalDialogFragment : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val stockRetrievalSettings = StockRetrievalSettings(requireContext())
        val dialogRootView: View = LayoutInflater.from(context)
                .inflate(R.layout.dialog_stock_retrieval_time_intervals, view as ViewGroup?, false)
        val currentSettings = stockRetrievalSettings.timeInterval.value as StockRetrievalSettings.ViewState.VALUES
        val inputFromTime: EditText = dialogRootView
                .findViewById(R.id.editText_dialogStockRetrievalTimeIntervals_fromTime)
        val inputLayoutFromTime: TextInputLayout = dialogRootView
                .findViewById(R.id.textInputLayout_dialogStockRetrievalTimeIntervals_fromTime)
        initFromTimePicker(currentSettings, inputFromTime, inputLayoutFromTime)
        val inputToTime: EditText = dialogRootView
                .findViewById(R.id.editText_dialogStockRetrievalTimeIntervals_toTime)
        val inputLayoutToTime: TextInputLayout = dialogRootView
                .findViewById(R.id.textInputLayout_dialogStockRetrievalTimeIntervals_toTime)
        initToTimePicker(currentSettings, inputToTime, inputLayoutToTime)
        val dayPicker = dialogRootView.findViewById<MaterialDayPicker>(R.id.materialDayPicker_dialogStockRetrievalTimeIntervals)
        initDayPicker(currentSettings, dayPicker)
        val context = requireContext()
        val confirmListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
        }
        val alertDialog = AlertDialog.Builder(context)
                .setMessage(R.string.settings_dialog_time_intervals_title)
                .setView(dialogRootView)
                .setNegativeButton(getText(R.string.action_cancel)) { _, _ ->
                    dismiss()
                }
                .setPositiveButton(getText(R.string.action_ok), confirmListener)
                .create()
        alertDialog.setOnShowListener {
            val button: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                try {
                    validateTime(inputFromTime, inputLayoutFromTime)
                    validateTime(inputToTime, inputLayoutToTime)
                    val fromTimeAsString = inputFromTime.text.toString()
                    val toTimeAsString = inputToTime.text.toString()
                    val fromTime = SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).parse(fromTimeAsString)!!
                    val toTime = SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).parse(toTimeAsString)!!
                    if (toTime.before(fromTime)) {
                        inputToTime.error = getString(R.string.generic_error_invalid_time_interval)
                        inputToTime.requestFocus()
                        throw ValidatorException()
                    }
                    stockRetrievalSettings.set(fromTime.hours, fromTime.minutes, toTime.hours, toTime.minutes,
                            dayPicker.selectedDays.map { materialWeekday -> DayOfWeek.valueOf(materialWeekday.name) })
                    Toast.makeText(requireContext(), getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                } catch (e: ValidatorException) {
                    // Shit happens ...
                }
            }
        }
        return alertDialog
    }

    private fun initFromTimePicker(
            settings: StockRetrievalSettings.ViewState.VALUES,
            inputFromTime: EditText,
            inputLayoutFromTime: TextInputLayout,
    ) {
        inputFromTime.setText(String.format("%s:%s", formatHoursAsString(settings.fromTimeHours),
                formatMinutesAsString(settings.fromTimeMinutes)))
        inputFromTime.transformIntoTimePicker(requireContext(), TIME_FORMAT, inputLayoutFromTime)
    }

    private fun initToTimePicker(
            settings: StockRetrievalSettings.ViewState.VALUES,
            inputToTime: EditText,
            inputLayoutToTime: TextInputLayout,
    ) {
        inputToTime.setText(String.format("%s:%s", formatHoursAsString(settings.toTimeHours),
                formatMinutesAsString(settings.toTimeMinutes)))
        inputToTime.transformIntoTimePicker(requireContext(), TIME_FORMAT, inputLayoutToTime)
    }

    private fun initDayPicker(currentSettings: StockRetrievalSettings.ViewState.VALUES, dayPicker: MaterialDayPicker) {
        dayPicker.setSelectedDays(currentSettings.weekDays.map { dayOfWeek -> MaterialDayPicker.Weekday.valueOf(dayOfWeek.name) })
        dayPicker.firstDayOfWeek = MaterialDayPicker.Weekday.MONDAY
    }

    private fun formatMinutesAsString(minutes: Int): String {
        return if (minutes < 10) {
            "0${minutes}"
        } else {
            "$minutes"
        }
    }

    private fun formatHoursAsString(hours: Int): String {
        return if (hours < 10) {
            "0${hours}"
        } else {
            "$hours"
        }
    }

    private fun validateTime(input: EditText, inputLayout: TextInputLayout) {
        when {
            input.text.toString().isEmpty() -> {
                inputLayout.error = getString(R.string.generic_error_empty)
                input.requestFocus()
                throw ValidatorException()
            }
            SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).parse(input.text.toString()) == null -> {
                inputLayout.error = getString(R.string.generic_error_invalid_date)
                input.requestFocus()
                throw ValidatorException()
            }
            else -> {
                inputLayout.error = null
            }
        }
    }

    class ValidatorException : Exception()

}
