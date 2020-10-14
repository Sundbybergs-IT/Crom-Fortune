package com.sundbybergsit.cromfortune.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.sundbybergsit.cromfortune.R

class AddStockDialogFragment(private val homeViewModel: HomeViewModel) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogRootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_add_stock, view as ViewGroup?, false)
        val inputStockQuantity: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_quantityInput)
        val inputStockPrice: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_priceInput)
        val inputStockName: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_nameInput)
        val confirmListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
            val stockOrder = StockOrder(inputStockName.text.toString(), inputStockPrice.text.toString().toDouble(),
                    inputStockQuantity.text.toString().toInt())
            homeViewModel.save(requireContext(), stockOrder)
        }
        return  AlertDialog.Builder(requireContext())
                .setView(dialogRootView)
                .setMessage(R.string.home_add_stock_message)
                .setNegativeButton(getText(R.string.action_cancel)) { _, _ ->
                    Toast.makeText(requireContext(),
                            getText(R.string.generic_error_not_supported), Toast.LENGTH_LONG).show()
                }
                .setPositiveButton(getText(R.string.action_ok), confirmListener)
                .create()
    }

}
