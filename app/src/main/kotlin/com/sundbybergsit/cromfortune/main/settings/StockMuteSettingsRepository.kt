package com.sundbybergsit.cromfortune.main.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sundbybergsit.cromfortune.main.ui.settings.StockMuteSettings

const val PREFERENCES_NAME = "StockMuteSettings"

object StockMuteSettingsRepository {

    private const val TAG = "StockMuteSettingsRepository"

    private lateinit var sharedPreferences: SharedPreferences

    private val _stockMuteSettings : MutableState<Collection<StockMuteSettings>> = mutableStateOf(emptyList())

    val STOCK_MUTE_MUTE_SETTINGS: State<Collection<StockMuteSettings>> = _stockMuteSettings

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        _stockMuteSettings.value = list()
    }

    @SuppressLint("ApplySharedPref")
    fun mute(stockSymbol: String) {
        Log.v(TAG, "mute(${stockSymbol})")
        sharedPreferences.edit().putString(stockSymbol, true.toString()).commit()
        val result: Collection<StockMuteSettings> = sharedPreferences.all
                .map { entry -> StockMuteSettings(entry.key, (entry.value as String).toBoolean()) }
        _stockMuteSettings.value = result
    }

    @SuppressLint("ApplySharedPref")
    fun unmute(stockSymbol: String) {
        Log.v(TAG, "unmute(${stockSymbol})")
        sharedPreferences.edit().putString(stockSymbol, false.toString()).commit()
        val result: Collection<StockMuteSettings> = sharedPreferences.all
                .map { entry -> StockMuteSettings(entry.key, (entry.value as String).toBoolean()) }
        _stockMuteSettings.value = result
    }

    fun list(): Collection<StockMuteSettings> {
        return sharedPreferences.all.map { entry -> StockMuteSettings(entry.key, (entry.value as String).toBoolean()) }
    }

    fun isMuted(stockSymbol: String): Boolean {
        val value = sharedPreferences.getString(stockSymbol, false.toString())
        return value != null && value.toBoolean()
    }

}
