package com.sundbybergsit.cromfortune.ui.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.sundbybergsit.cromfortune.R

class SupportedStockDialogFragment(private val viewModel: SettingsViewModel) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val allStocks = viewModel.allStocks(context)
        var message = ""
        for (stock in allStocks) {
            message += "$stock, "
        }
        return AlertDialog.Builder(context)
                .setMessage(message)
                .setNegativeButton(getText(R.string.action_cancel)) { _, _ ->
                    Toast.makeText(context,
                            getText(R.string.generic_error_not_supported), Toast.LENGTH_LONG).show()
                }
                .setPositiveButton(getText(R.string.action_close)) { _, _ -> }
                .create()
    }

}
