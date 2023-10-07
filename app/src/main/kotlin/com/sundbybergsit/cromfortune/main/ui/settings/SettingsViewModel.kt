package com.sundbybergsit.cromfortune.main.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    private val _todoText: MutableStateFlow<String> = MutableStateFlow("")

    val todoText: StateFlow<String> = _todoText.asStateFlow()

}
