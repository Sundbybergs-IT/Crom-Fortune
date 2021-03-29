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
                "1. Lägg till anteckningar per aktie\n" +
                "2. Fixa 'bugg' att refresh görs i två omgångar (valuta / kurs)\n" +
                "3. Dark theme vid vissa klockslag\n" +
                "4. Hem: Implementera köp/sälj-knappar\n" +
                "5. Hem: Sortering (bäst/sämst/ABC)\n" +
                "6. Inställningar: Lägg till alarm\n" +
                "7. Hem: Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge)\n" +
                "8. Hem: Lägg till stöd för utdelningar\n" +
                "9. Hem: Lägg till stöd för nyemissioner\n" +
                "10. Hem: Lägg till stöd för omvända splitar\n" +
                "11. Inställningar: Implementera stöd för individuell courtageplan\n" +
                "12. Instrumentbräda: Exkludera transaktioner från 0 -> x aktier från Crom conformance score\n" +
                "13. Notifieringar: Lägg till stöd för att ta bort enskilda notifieringar\n"
        val context = requireContext()
        return AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(getText(R.string.action_close)) { _, _ -> }
                .create()
    }

}
