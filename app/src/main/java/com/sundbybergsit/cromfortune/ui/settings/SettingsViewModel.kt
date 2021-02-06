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
        value = "Att göra: 1. Rekursion på Croms rekommendationer, " +
                "2. Fixa så notifieringar skapas utan att man går in i dashboard " +
                "3. Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge) " +
                "4. Home: Lägg till sorteringsmöjligheter (Bäst idag, sämst idag, alfabetisk sortering) " +
                "5. Dashboard: Lägg till Croms skugghandel " +
                "6. Home: Implementera köp/sälj-knappar" +
                "7. Dark theme vid vissa klockslag"
    }
    val todoText: LiveData<String> = _todoText

}
