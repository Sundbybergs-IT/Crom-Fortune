package com.sundbybergsit.cromfortune.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sundbybergsit.cromfortune.ui.settings.StockMuteSettings

const val PREFERENCES_NAME = "StockMuteSettings"

object StockMuteSettingsRepository {

    private const val TAG = "StockMuteSettingsRepository"

    private lateinit var sharedPreferences: SharedPreferences

    @Suppress("ObjectPropertyName")
    private val _stockMuteSettings = MutableLiveData<Collection<StockMuteSettings>>(emptyList())

    val STOCK_MUTE_MUTE_SETTINGS: LiveData<Collection<StockMuteSettings>> = _stockMuteSettings

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        _stockMuteSettings.postValue(list())
    }

    @SuppressLint("ApplySharedPref")
    fun mute(stockSymbol: String) {
        Log.v(TAG, "mute(${stockSymbol})")
        sharedPreferences.edit().putString(stockSymbol, true.toString()).commit()
        val result: Collection<StockMuteSettings> = sharedPreferences.all
                .map { entry -> StockMuteSettings(entry.key, (entry.value as String).toBoolean()) }
        _stockMuteSettings.postValue(result)
    }

    @SuppressLint("ApplySharedPref")
    fun unmute(stockSymbol: String) {
        Log.v(TAG, "unmute(${stockSymbol})")
        sharedPreferences.edit().putString(stockSymbol, false.toString()).commit()
        val result: Collection<StockMuteSettings> = sharedPreferences.all
                .map { entry -> StockMuteSettings(entry.key, (entry.value as String).toBoolean()) }
        _stockMuteSettings.postValue(result)
    }

    fun list(): Collection<StockMuteSettings> {
        return sharedPreferences.all.map { entry -> StockMuteSettings(entry.key, (entry.value as String).toBoolean()) }
    }

    fun isMuted(stockSymbol: String): Boolean {
        val value = sharedPreferences.getString(stockSymbol, false.toString())
        return value != null && value.toBoolean()
    }

}
