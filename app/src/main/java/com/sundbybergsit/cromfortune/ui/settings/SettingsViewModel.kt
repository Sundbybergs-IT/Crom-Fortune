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
                "1. Förbättra beräkning av GAV att inte ta hänsyn till gamla irrelevanta transaktioner, " +
                "2. Ställ in när appen ska 'sova' och inte hetsa om rekommendationer, " +
                "3. Home: Lägg till sorteringsmöjligheter (Bäst idag, sämst idag, alfabetisk sortering), " +
                "4. Home: Implementera köp/sälj-knappar, " +
                "5. Humpnotiser om en aktie (som man inte har) har en kurs som är under senaste säljorderns kurs,  " +
                "6 Dashboard: Lägg till Croms skugghandel, " +
                "7. Snygga till Croms 'compliance score', " +
                "8. Dark theme vid vissa klockslag, " +
                "9. Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge), " +
                "10. Home: Total vinst"
    }
    val todoText: LiveData<String> = _todoText

}
