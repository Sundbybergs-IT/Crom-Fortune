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
        value = "Att göra: 1. Home: Lägg till vinst-kolumn " +
                "2. Rekursion på Croms rekommendationer, " +
                "3. Fixa så notifieringar skapas utan att man går in i dashboard " +
                "4. Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge) " +
                "5. Home: Lägg till sorteringsmöjligheter (Bäst idag, sämst idag, alfabetisk sortering) " +
                "6. Dashboard: Lägg till Croms skugghandel " +
                "7. Home: Implementera köp/sälj-knappar"
    }
    val todoText: LiveData<String> = _todoText

}
