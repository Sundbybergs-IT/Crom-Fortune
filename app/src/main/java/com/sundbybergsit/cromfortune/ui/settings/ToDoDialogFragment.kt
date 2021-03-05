package com.sundbybergsit.cromfortune.ui.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.sundbybergsit.cromfortune.R

class ToDoDialogFragment : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = "Att göra:\n" +
                "1. Notifieringar: Lägg till arkiv\n" +
                "2. Notiferingar: Lägg till humpnotiser då en kurs är lägre än senaste sälj (vid 0 aktier)\n" +
                "3. Lägg till anteckningar per aktie\n" +
                "4. Fixa 'bugg' att refresh görs i två omgångar (valuta / kurs)\n" +
                "5. Dark theme vid vissa klockslag\n" +
                "6. Hem: Implementera köp/sälj-knappar\n" +
                "7. Hem: Sortering (bäst/sämst/ABC)\n" +
                "8. Inställningar: Lägg till alarm\n" +
                "9. Hem: Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge)\n" +
                "10. Hem: Lägg till stöd för utdelningar\n" +
                "11. Hem: Lägg till stöd för nyemissioner\n" +
                "12. Hem: Lägg till stöd för omvända splitar\n" +
                "13. Inställningar: Implementera stöd för individuell courtageplan\n" +
                "14. Instrumentbräda: Exkludera transaktioner från 0 -> x aktier från Crom conformance score\n"
        val context = requireContext()
        return AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(getText(R.string.action_close)) { _, _ -> }
                .create()
    }

}
