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
                "1. Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge), " +
                "2. Förbättra beräkning av GAV att inte ta hänsyn till gamla irrelevanta transaktioner, " +
                "3. Home: Lägg till sorteringsmöjligheter (Bäst idag, sämst idag, alfabetisk sortering), " +
                "4. Snygga till Croms 'compliance score', " +
                "5. Rekursion på Croms rekommendationer, "
                "6. Dashboard: Lägg till Croms skugghandel, " +
                "7. Home: Implementera köp/sälj-knappar, " +
                "8. Dark theme vid vissa klockslag, " +
                "9. Home: Total vinst vid vissa klockslag, "
    }
    val todoText: LiveData<String> = _todoText

}
