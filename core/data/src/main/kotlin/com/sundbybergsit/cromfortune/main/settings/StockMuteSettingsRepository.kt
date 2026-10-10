package com.sundbybergsit.cromfortune.main.settings

import android.util.Log
import android.content.Context
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import com.sundbybergsit.cromfortune.main.db.StockMuteSettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StockMuteSettingsRepository {

    private const val TAG = "StockMuteSettingsRepository"

    private lateinit var appContext: Context

    private val _stockMuteSettings = MutableStateFlow<Collection<StockMuteSettings>>(emptyList())

    val STOCK_MUTE_MUTE_SETTINGS: StateFlow<Collection<StockMuteSettings>> = _stockMuteSettings.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        _stockMuteSettings.value = list()
    }

    fun mute(stockSymbol: String) {
        Log.v(TAG, "mute(${stockSymbol})")
        val dao = CromFortuneDatabase.getInstance(appContext).stockMuteSettingsDao()
        dao.insert(StockMuteSettingsEntity(stockSymbol, true))
        _stockMuteSettings.value = list()
    }

    fun unmute(stockSymbol: String) {
        Log.v(TAG, "unmute(${stockSymbol})")
        val dao = CromFortuneDatabase.getInstance(appContext).stockMuteSettingsDao()
        dao.insert(StockMuteSettingsEntity(stockSymbol, false))
        _stockMuteSettings.value = list()
    }

    fun list(): Collection<StockMuteSettings> {
        val dao = CromFortuneDatabase.getInstance(appContext).stockMuteSettingsDao()
        return dao.getAllMuteSettings().map { StockMuteSettings(it.stockSymbol, it.muted) }
    }

    fun isMuted(stockSymbol: String): Boolean {
        val dao = CromFortuneDatabase.getInstance(appContext).stockMuteSettingsDao()
        val setting = dao.getAllMuteSettings().find { it.stockSymbol == stockSymbol }
        return setting?.muted == true
    }

}
