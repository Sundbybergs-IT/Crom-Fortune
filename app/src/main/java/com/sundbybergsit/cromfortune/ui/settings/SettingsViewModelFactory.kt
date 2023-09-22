package com.sundbybergsit.cromfortune.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel() as T
    }

}
