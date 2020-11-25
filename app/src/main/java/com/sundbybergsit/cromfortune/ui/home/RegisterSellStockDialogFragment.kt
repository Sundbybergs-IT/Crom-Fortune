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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.transformIntoDatePicker
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "MM/dd/yyyy"

class RegisterSellStockDialogFragment(private val homeViewModel: HomeViewModel) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogRootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_remove_stock, view as ViewGroup?, false)
        val inputCurrency: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogRemoveStock_currencyInput)
        inputCurrency.setAdapter(getCurrencyAutoCompleteAdapter())
        val inputLayoutCurrency: TextInputLayout = dialogRootView.findViewById(R.id.textInputLayout_dialogRemoveStock_currencyInput)
        val inputDate: EditText = dialogRootView.findViewById(R.id.editText_dialogRemoveStock_dateInput)
        val inputLayoutDate: TextInputLayout = dialogRootView.findViewById(R.id.textInputLayout_dialogRemoveStock_dateInput)
        inputDate.transformIntoDatePicker(requireContext(), DATE_FORMAT, Date(), inputLayoutDate)
        val inputStockQuantity: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogRemoveStock_quantityInput)
        val inputLayoutStockQuantity: TextInputLayout = dialogRootView.findViewById(R.id.textInputLayout_dialogRemoveStock_quantityInput)
        val inputStockPrice: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogRemoveStock_priceInput)
        val inputLayoutStockPrice: TextInputLayout = dialogRootView.findViewById(R.id.textInputLayout_dialogRemoveStock_priceInput)
        val inputStockName: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogRemoveStock_nameInput)
        inputStockName.setAdapter(getStockNameAutoCompleteAdapter())
        val inputLayoutStockName: TextInputLayout = dialogRootView.findViewById(R.id.textInputLayout_dialogRemoveStock_nameInput)
        val inputCommissionFee: AutoCompleteTextView = dialogRootView.findViewById(R.id.autoCompleteTextView_dialogRemoveStock_commissionFeeInput)
        val inputLayoutCommissionFee: TextInputLayout = dialogRootView.findViewById(R.id.textInputLayout_dialogRemoveStock_commissionFeeInput)
        val confirmListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
        }
        val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogRootView)
                .setMessage(R.string.home_remove_stock_message)
                .setNegativeButton(getText(R.string.action_cancel)) { _, _ ->
                    Toast.makeText(requireContext(),
                            getText(R.string.generic_error_not_supported), Toast.LENGTH_LONG).show()
                }
                .setPositiveButton(getText(R.string.action_ok), confirmListener)
                .create()
        alertDialog.setOnShowListener {
            val button: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                try {
                    validateDate(inputDate, inputLayoutDate)
                    validateCurrency(inputCurrency, inputLayoutCurrency)
                    validateDouble(inputStockQuantity, inputLayoutStockQuantity)
                    validateStockName(inputStockName, inputLayoutStockName)
                    validateDouble(inputStockPrice, inputLayoutStockPrice)
                    validateDouble(inputCommissionFee, inputLayoutCommissionFee)
                    val dateAsString = inputDate.text.toString()
                    val date = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateAsString)
                    val currency = Currency.getInstance(inputCurrency.text.toString())
                    // TODO: Convert commission fee (in SEK) to selected currency
                    val stockOrder = StockOrder("Sell", currency.toString(), date.time, inputStockName.text.toString(),
                            inputStockPrice.text.toString().toDouble(), inputCommissionFee.text.toString().toDouble(),
                            inputStockQuantity.text.toString().toInt())
                    homeViewModel.save(requireContext(), stockOrder)
                    alertDialog.dismiss()
                } catch (e : ValidatorException) {
                    // Shit happens ...
                }
            }
        }
        return alertDialog
    }

    private fun getCurrencyAutoCompleteAdapter() : AutoCompleteAdapter {
        val searchArrayList = ArrayList(StockPriceRetriever.CURRENCIES.toList())
        return AutoCompleteAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
                android.R.id.text1, searchArrayList)
    }

    private fun getStockNameAutoCompleteAdapter() : AutoCompleteAdapter {
        val searchArrayList = ArrayList(StockPriceRetriever.SYMBOLS.toList())
        return AutoCompleteAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
                android.R.id.text1, searchArrayList)
    }

    private fun validateCurrency(input: AutoCompleteTextView, inputLayout: TextInputLayout) {
        when {
            input.text.toString().isEmpty() -> {
                inputLayout.error = getString(R.string.generic_error_empty)
                input.requestFocus()
                throw ValidatorException()
            }
            !StockPriceRetriever.CURRENCIES.contains(input.text.toString()) -> {
                inputLayout.error = getString(R.string.generic_error_invalid_stock_symbol)
                input.requestFocus()
                throw ValidatorException()
            }
            else -> {
                inputLayout.error = null
            }
        }
    }

    private fun validateStockName(input: AutoCompleteTextView, inputLayout: TextInputLayout) {
        when {
            input.text.toString().isEmpty() -> {
                inputLayout.error = getString(R.string.generic_error_empty)
                input.requestFocus()
                throw ValidatorException()
            }
            !StockPriceRetriever.SYMBOLS.contains(input.text.toString()) -> {
                inputLayout.error = getString(R.string.generic_error_invalid_stock_symbol)
                input.requestFocus()
                throw ValidatorException()
            }
            else -> {
                inputLayout.error = null
            }
        }
    }

    private fun validateDate(input: EditText, inputLayout: TextInputLayout) {
        when {
            input.text.toString().isEmpty() -> {
                inputLayout.error = getString(R.string.generic_error_empty)
                input.requestFocus()
                throw ValidatorException()
            }
            SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(input.text.toString()) == null -> {
                inputLayout.error = getString(R.string.generic_error_invalid_date)
                input.requestFocus()
                throw ValidatorException()
            }
            else -> {
                inputLayout.error = null
            }
        }
    }

    private fun validateDouble(input: AutoCompleteTextView, inputLayout: TextInputLayout) {
        when {
            input.text.toString().isEmpty() -> {
                inputLayout.error = getString(R.string.generic_error_empty)
                input.requestFocus()
                throw ValidatorException()
            }
            input.text.toString().toDoubleOrNull() == null -> {
                inputLayout.error = getString(R.string.generic_error_invalid_number)
                input.requestFocus()
                throw ValidatorException()
            }
            else -> {
                inputLayout.error = null
            }
        }
    }

    class ValidatorException : Exception()

}
