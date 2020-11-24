package com.sundbybergsit.cromfortune.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Att göra: Stöd för att sälja, stöd för flexibel courtageavgift för rekommendationer, stöd för att ställa in standardvärden" +
                "pollning 1 gång per dygn och lagring av pollade värden, insamling av historisk data sen första köpet," +
                "en lista över alla aktiesymboler, notifieringar..."
    }
    val text: LiveData<String> = _text
}
