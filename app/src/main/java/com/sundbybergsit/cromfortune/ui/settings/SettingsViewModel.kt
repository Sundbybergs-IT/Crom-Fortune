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
        value = "Att göra: " +
                "1. Fyll i valutafältet automatiskt vid val av aktier, " +
                "2. Förbättra beräkning av GAV att inte ta hänsyn till gamla irrelevanta transaktioner, " +
                "3. Home: Lägg till sorteringsmöjligheter (Bäst idag, sämst idag, alfabetisk sortering), " +
                "4. Home: Implementera köp/sälj-knappar, " +
                "5. Dashboard: Lägg till Croms skugghandel, " +
                "6. Snygga till Croms 'compliance score', " +
                "7. Dark theme vid vissa klockslag, " +
                "8. Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge), " +
                "9. Home: Total vinst"
    }
    val todoText: LiveData<String> = _todoText

}
