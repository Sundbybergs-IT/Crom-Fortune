package com.sundbybergsit.cromfortune.main.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.themeDataStore by preferencesDataStore(name = "ThemeSettings")

object ThemeSettingsRepository {
    private val THEME_MODE = stringPreferencesKey("theme_mode")

    enum class ThemeMode {
        SYSTEM, LIGHT, DARK
    }

    private lateinit var appContext: Context
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        runBlocking {
            val prefs = appContext.themeDataStore.data.first()
            val mode = prefs[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
            _themeMode.value = mode
        }
        CoroutineScope(Dispatchers.IO).launch {
            appContext.themeDataStore.data.collect { prefs ->
                val mode = prefs[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
                _themeMode.value = mode
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        CoroutineScope(Dispatchers.IO).launch {
            appContext.themeDataStore.edit { prefs ->
                prefs[THEME_MODE] = mode.name
            }
        }
    }
}
