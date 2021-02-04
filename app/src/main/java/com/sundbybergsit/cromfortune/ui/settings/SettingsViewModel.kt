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
        value = "Att göra: 1. Fixa rekommendationsbugg (vill att man köper till högre kurs) " +
                "2. Home: Möjlighet att radera en transaktion " +
                "3. Home: Lägg till vinst-kolumn " +
                "4. Rekursion på Croms rekommendationer, " +
                "5. Fixa så notifieringar skapas utan att man går in i dashboard " +
                "6. Home: Lägg till sorteringsmöjligheter (Bäst idag, sämst idag, alfabetisk sortering) " +
                "7. Dashboard: Lägg till Croms skugghandel " +
                "8. Home: Implementera köp/sälj-knappar"
    }
    val todoText: LiveData<String> = _todoText

}
