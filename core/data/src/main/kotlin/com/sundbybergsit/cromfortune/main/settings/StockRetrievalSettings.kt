package com.sundbybergsit.cromfortune.main.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek

class StockRetrievalSettings(
    context: Context,
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
) {

    companion object {

        const val PREFERENCES_NAME = "StockRetrievalSettings"
        private const val TAG = "StockRetrievalSettings"
        private const val REFRESH_INTERVAL_MINUTES = "refreshIntervalMinutes"

    }

    private val _timeInterval = MutableStateFlow(getValuesFromDb())

    val timeInterval: StateFlow<ViewState> = _timeInterval.asStateFlow()

    fun set(fromTimeHours: Int, fromTimeMinutes: Int, toTimeHours: Int, toTimeMinutes: Int, weekDays: List<DayOfWeek>) =
        set(fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays, _timeInterval.value.refreshInterval)

    fun set(
        fromTimeHours: Int,
        fromTimeMinutes: Int,
        toTimeHours: Int,
        toTimeMinutes: Int,
        weekDays: List<DayOfWeek>,
        refreshInterval: RefreshInterval
    ) {
        Log.v(
            TAG,
            "set(fromTimeHours=[${fromTimeHours}],fromTimeMinutes=[${fromTimeMinutes}], " +
                "toTimeHours=[${toTimeHours}], toTimeMinutes=[${toTimeMinutes}], weekDays=[${weekDays}])"
        )

        sharedPreferences.edit().putInt("fromTimeHours", fromTimeHours).putInt("fromTimeMinutes", fromTimeMinutes)
            .putInt("toTimeHours", toTimeHours).putInt("toTimeMinutes", toTimeMinutes)
            .putStringSet("weekDays", weekDays.map { weekDay -> weekDay.name }.toSet())
            .putInt(REFRESH_INTERVAL_MINUTES, refreshInterval.minutes)
            .apply()
        _timeInterval.value = ViewState(
            fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays, refreshInterval
        )
    }

    private fun getValuesFromDb(): ViewState {
        val fromTimeHours = sharedPreferences.getInt("fromTimeHours", 0)
        val fromTimeMinutes = sharedPreferences.getInt("fromTimeMinutes", 0)
        val toTimeHours = sharedPreferences.getInt("toTimeHours", 23)
        val toTimeMinutes = sharedPreferences.getInt("toTimeMinutes", 59)
        val weekDays = sharedPreferences.getStringSet(
            "weekDays",
            setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
        )!!
            .map { stringRepresentation ->
                DayOfWeek.valueOf(stringRepresentation)
            }

        val refreshInterval = RefreshInterval.fromMinutes(
            sharedPreferences.getInt(REFRESH_INTERVAL_MINUTES, RefreshInterval.ONE_HOUR.minutes)
        )

        return ViewState(
            fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays.toList(), refreshInterval
        )
    }

    class ViewState(
        val fromTimeHours: Int, val fromTimeMinutes: Int, val toTimeHours: Int, val toTimeMinutes: Int,
        val weekDays: List<DayOfWeek>,
        val refreshInterval: RefreshInterval,
    )

    enum class RefreshInterval(val minutes: Int) {
        FIFTEEN_MINUTES(15),
        THIRTY_MINUTES(30),
        ONE_HOUR(60),
        THREE_HOURS(180),
        SIX_HOURS(360),
        TWELVE_HOURS(720);

        companion object {
            fun fromMinutes(minutes: Int): RefreshInterval =
                entries.firstOrNull { it.minutes == minutes } ?: ONE_HOUR
        }
    }

}
