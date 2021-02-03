package com.sundbybergsit.cromfortune.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is settings Fragment"
    }
    val text: LiveData<String> = _text

    private val _todoText = MutableLiveData<String>().apply {
        value = "Att göra: 1. Rekursion på Croms rekommendationer, 2. Jämför utfall med Crom"
    }
    val todoText: LiveData<String> = _todoText

}
