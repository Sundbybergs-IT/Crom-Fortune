package com.sundbybergsit.cromfortune.ui.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _todoText: MutableState<String> = mutableStateOf("")

    val todoText: State<String> = _todoText

}
