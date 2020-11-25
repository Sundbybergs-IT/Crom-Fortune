package com.sundbybergsit.cromfortune.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Att göra: Stöd för att ställa in standardvärden (courtage och valuta), " +
                "Ändra pollning till 1 gång per timme, lagring av rekommendationer, logik för att kontrollera om rekommendationer har följts, analys av historisk data sen första köpet," +
                "en lista över alla aktiesymboler, notifieringar..."
    }
    val text: LiveData<String> = _text
}
