package com.sundbybergsit.cromfortune.main.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek

private val Context.dataStore by preferencesDataStore(name = StockRetrievalSettings.PREFERENCES_NAME)

class StockRetrievalSettings(
    private val context: Context,
    @Suppress("UNUSED_PARAMETER")
    private val sharedPreferences: Any? = null
) {

    companion object {

        const val PREFERENCES_NAME = "StockRetrievalSettings"
        private const val TAG = "StockRetrievalSettings"
        private const val REFRESH_INTERVAL_MINUTES = "refreshIntervalMinutes"

        private val FROM_TIME_HOURS = intPreferencesKey("fromTimeHours")
        private val FROM_TIME_MINUTES = intPreferencesKey("fromTimeMinutes")
        private val TO_TIME_HOURS = intPreferencesKey("toTimeHours")
        private val TO_TIME_MINUTES = intPreferencesKey("toTimeMinutes")
        private val WEEK_DAYS = stringSetPreferencesKey("weekDays")
        private val REFRESH_INTERVAL = intPreferencesKey(REFRESH_INTERVAL_MINUTES)

    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _timeInterval = MutableStateFlow(getValuesFromDb())

    val timeInterval: StateFlow<ViewState> = _timeInterval.asStateFlow()

    init {
        scope.launch {
            context.dataStore.data.collect { prefs ->
                val fromTimeHours = prefs[FROM_TIME_HOURS] ?: 0
                val fromTimeMinutes = prefs[FROM_TIME_MINUTES] ?: 0
                val toTimeHours = prefs[TO_TIME_HOURS] ?: 23
                val toTimeMinutes = prefs[TO_TIME_MINUTES] ?: 59
                val weekDays = prefs[WEEK_DAYS]?.map { DayOfWeek.valueOf(it) }
                    ?: listOf(
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                    )
                val refreshInterval = RefreshInterval.fromMinutes(
                    prefs[REFRESH_INTERVAL] ?: RefreshInterval.ONE_HOUR.minutes
                )
                _timeInterval.value = ViewState(
                    fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays, refreshInterval
                )
            }
        }
    }

    fun clear(context: Context) {
        runBlocking {
            context.dataStore.edit { it.clear() }
        }
        _timeInterval.value = getValuesFromDb()
    }

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

        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[FROM_TIME_HOURS] = fromTimeHours
                prefs[FROM_TIME_MINUTES] = fromTimeMinutes
                prefs[TO_TIME_HOURS] = toTimeHours
                prefs[TO_TIME_MINUTES] = toTimeMinutes
                prefs[WEEK_DAYS] = weekDays.map { weekDay -> weekDay.name }.toSet()
                prefs[REFRESH_INTERVAL] = refreshInterval.minutes
            }
        }
        _timeInterval.value = ViewState(
            fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays, refreshInterval
        )
    }

    private fun getValuesFromDb(): ViewState {
        return runBlocking {
            val prefs = runCatching { context.dataStore.data.first() }.getOrNull()
            val fromTimeHours = prefs?.get(FROM_TIME_HOURS) ?: 0
            val fromTimeMinutes = prefs?.get(FROM_TIME_MINUTES) ?: 0
            val toTimeHours = prefs?.get(TO_TIME_HOURS) ?: 23
            val toTimeMinutes = prefs?.get(TO_TIME_MINUTES) ?: 59
            val weekDays = prefs?.get(WEEK_DAYS)?.map { stringRepresentation ->
                DayOfWeek.valueOf(stringRepresentation)
            } ?: listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )

            val refreshInterval = RefreshInterval.fromMinutes(
                prefs?.get(REFRESH_INTERVAL) ?: RefreshInterval.ONE_HOUR.minutes
            )

            ViewState(
                fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes, weekDays.toList(), refreshInterval
            )
        }
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
