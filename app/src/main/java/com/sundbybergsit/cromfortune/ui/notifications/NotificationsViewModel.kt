package com.sundbybergsit.cromfortune.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Att göra: Stöd för att sälja, Stöd för fler valutor än SEK, stöd för flexibel courtageavgift för rekommendationer, " +
                "pollning 1 gång per dygn och lagring av pollade värden, insamling av historisk data sen första köpet"
    }
    val text: LiveData<String> = _text
}
