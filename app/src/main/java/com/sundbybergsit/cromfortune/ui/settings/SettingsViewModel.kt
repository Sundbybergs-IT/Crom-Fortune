package com.sundbybergsit.cromfortune.ui.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    val _showStockRetrievalTimeIntervalsDialog: MutableState<Boolean> = mutableStateOf(false)
    val showStockRetrievalTimeIntervalsDialog: State<Boolean> = _showStockRetrievalTimeIntervalsDialog
    val _showSupportedStocksDialog: MutableState<Boolean> = mutableStateOf(false)
    val showSupportedStocksDialog: State<Boolean> = _showSupportedStocksDialog

    private val _text: MutableState<String> = mutableStateOf("This is settings Fragment")
    val text: State<String> = _text

    private val _todoText: MutableState<String> = mutableStateOf("")

    val todoText: State<String> = _todoText

}
