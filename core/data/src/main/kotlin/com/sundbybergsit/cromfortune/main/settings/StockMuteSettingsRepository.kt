package com.sundbybergsit.cromfortune.main.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val PREFERENCES_NAME = "StockMuteSettings"

object StockMuteSettingsRepository {

    private const val TAG = "StockMuteSettingsRepository"

    private lateinit var sharedPreferences: SharedPreferences

    private val _stockMuteSettings = MutableStateFlow<Collection<StockMuteSettings>>(emptyList())

    val STOCK_MUTE_MUTE_SETTINGS: StateFlow<Collection<StockMuteSettings>> = _stockMuteSettings.asStateFlow()

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
