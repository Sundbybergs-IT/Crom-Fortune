package com.sundbybergsit.cromfortune.feature.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _todoText = MutableStateFlow("")
    val todoText: StateFlow<String> = _todoText.asStateFlow()
}
