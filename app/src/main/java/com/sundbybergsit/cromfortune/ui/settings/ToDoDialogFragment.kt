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
                "1. Bugg: Inga rekommendationer när man bara har köpt aktier \n" +
                "2. Hem: Senaste kurs\n" +
                "3. Hem: Beräkna GAV vid 0 aktier\n" +
                "4. Hem: Mute/unmute aktie\n" +
                "5. Inställningar: Ställ in tidsintervall då kurser ska hämtas\n" +
                "6. Notifieringar: Lägg till arkiv\n" +
                "7. Notiferingar: Lägg till humpnotiser då en kurs är lägre än senaste sälj (vid 0 aktier)\n" +
                "8. Dark theme vid vissa klockslag\n" +
                "9. Hem: Implementera köp/sälj-knappar\n" +
                "10. Hem: Sortering (bäst/sämst/ABC)\n" +
                "11. Hem: Lägg till Croms skugghandel\n" +
                "12. Hem: Fixa refresh-bugg för när man har tagit bort en transaktion (byt tabb och tillbaka så länge)\n" +
                "13. Dashboard: Snygga till Croms compliance score\n" +
                "14. Inställningar: Implementera stöd för individuell courtageplan\n"
        val context = requireContext()
        return AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(getText(R.string.action_close)) { _, _ -> }
                .create()
    }

}
