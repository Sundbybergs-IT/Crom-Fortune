package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.transformIntoDatePicker
import yahoofinance.YahooFinance
import java.text.SimpleDateFormat
import java.util.*

class AddStockDialogFragment(private val homeViewModel: HomeViewModel) : DialogFragment() {

    // TODO: Implement support for multiple currencies
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogRootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_add_stock, view as ViewGroup?, false)
        val inputCurrency: EditText = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_currencyInput)
        inputCurrency.setText("SEK")
        val inputDate: EditText = dialogRootView.findViewById(R.id.editText_dialogAddStock_dateInput)
        inputDate.transformIntoDatePicker(requireContext(), "MM/dd/yyyy", Date())
        val inputStockQuantity: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_quantityInput)
        val inputStockPrice: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_priceInput)
        val inputStockName: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_nameInput)
        val searchArrayList = ArrayList(StockPriceRetriever.SYMBOLS.toList())
        val adapter = AutoCompleteAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, searchArrayList)
        inputStockName.setAdapter(adapter)
        val inputCommissionFee: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStock_commissionFeeInput)
        val currency = Currency.getInstance("SEK")
        val confirmListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
            val date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(inputDate.text.toString())
            if (date == null) {
                Toast.makeText(requireContext(), "Invalid date", Toast.LENGTH_LONG).show()
            } else {
                val stockOrder = StockOrder("Buy", currency.toString(), date.time, inputStockName.text.toString(),
                        inputStockPrice.text.toString().toDouble(), inputCommissionFee.text.toString().toDouble(),
                        inputStockQuantity.text.toString().toInt())
                homeViewModel.save(requireContext(), stockOrder)
            }
        }
        return AlertDialog.Builder(requireContext())
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
