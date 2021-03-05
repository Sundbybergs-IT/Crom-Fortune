package com.sundbybergsit.cromfortune.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

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

    fun set(fromTimeHours: Int, fromTimeMinutes: Int, toTimeHours: Int, toTimeMinutes: Int) {
        Log.v(TAG, "set(fromTimeHours=[${fromTimeHours}],fromTimeMinutes=[${fromTimeMinutes}], toTimeHours=[${toTimeHours}], toTimeMinutes=[${toTimeMinutes})")

        sharedPreferences.edit().putInt("fromTimeHours", fromTimeHours).putInt("fromTimeMinutes", fromTimeMinutes)
                .putInt("toTimeHours", toTimeHours).putInt("toTimeMinutes", toTimeMinutes).apply()
        _timeInterval.postValue(ViewState.VALUES(fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes))
    }

    private fun getValuesFromDb(): ViewState {
        val fromTimeHours = sharedPreferences.getInt("fromTimeHours", 0)
        val fromTimeMinutes = sharedPreferences.getInt("fromTimeMinutes", 0)
        val toTimeHours = sharedPreferences.getInt("toTimeHours", 23)
        val toTimeMinutes = sharedPreferences.getInt("toTimeMinutes", 59)
        return ViewState.VALUES(fromTimeHours, fromTimeMinutes, toTimeHours, toTimeMinutes)
    }

    sealed class ViewState {
        data class VALUES(val fromTimeHours: Int, val fromTimeMinutes: Int, val toTimeHours: Int, val toTimeMinutes: Int) : ViewState()
    }

}
