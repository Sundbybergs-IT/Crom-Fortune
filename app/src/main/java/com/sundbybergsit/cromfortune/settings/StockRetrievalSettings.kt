package com.sundbybergsit.cromfortune.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.DayOfWeek

class StockRetrievalSettings(
        context: Context,
        private val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
) {

    companion object {

        const val PREFERENCES_NAME = "StockRetrievalSettings"
        private const val TAG = "StockRetrievalSettings"

    }

    @Suppress("ObjectPropertyName")
    private val _timeInterval = MutableLiveData(getValuesFromDb())

    val timeInterval: LiveData<ViewState> = _timeInterval

    fun set(fromTimeHours: Int, fromTimeMinutes: Int, toTimeHours: Int, toTimeMinutes: Int, weekDays: List<DayOfWeek>) {
        Log.v(TAG, "set(fromTimeHours=[${fromTimeHours}],fromTimeMinutes=[${fromTimeMinutes}], toTimeHours=[${toTimeHours}], toTimeMinutes=[${toTimeMinutes})")

        sharedPreferences.edit().putInt("fromTimeHours", fromTimeHours).putInt("fromTimeMinutes", fromTimeMinutes)
                .putInt("toTimeHours", toTimeHours).putInt("toTimeMinutes", toTimeMinutes)
                .putStringSet("weekDays", weekDays.map { weekDay -> weekDay.name }.toSet()).apply()
        _timeInterval.postValue(ViewState.VALUES(fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays))
    }

    private fun getValuesFromDb(): ViewState {
        val fromTimeHours = sharedPreferences.getInt("fromTimeHours", 0)
        val fromTimeMinutes = sharedPreferences.getInt("fromTimeMinutes", 0)
        val toTimeHours = sharedPreferences.getInt("toTimeHours", 23)
        val toTimeMinutes = sharedPreferences.getInt("toTimeMinutes", 59)
        val weekDays = sharedPreferences.getStringSet("weekDays",
                setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"))!!
                .map { stringRepresentation ->
                    DayOfWeek.valueOf(stringRepresentation)
                }

        return ViewState.VALUES(fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays.toList())
    }

    sealed class ViewState {
        data class VALUES(
                val fromTimeHours: Int, val fromTimeMinutes: Int, val toTimeHours: Int, val toTimeMinutes: Int,
                val weekDays: List<DayOfWeek>,
        ) : ViewState()
    }

}
