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
                "3. Ställ in när appen ska 'sova' och inte hetsa om rekommendationer, " +
                "4. Home: Lägg till sorteringsmöjligheter (Bäst idag, sämst idag, alfabetisk sortering), " +
                "5. Home: Implementera köp/sälj-knappar, " +
                "6. Humpnotiser om en aktie (som man inte har) har en kurs som är under senaste säljorderns kurs,  "
                "7. Dashboard: Lägg till Croms skugghandel, " +
                "8. Snygga till Croms 'compliance score', " +
                "9. Dark theme vid vissa klockslag, " +
                "10. Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge), " +
                "11. Home: Total vinst"
    }
    val todoText: LiveData<String> = _todoText

}
