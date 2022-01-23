package com.sundbybergsit.cromfortune.ui.home.trade

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.ui.AutoCompleteAdapter
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.transformIntoDatePicker
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "MM/dd/yyyy"

class RegisterSplitDialogFragment(private val homeViewModel: HomeViewModel) : DialogFragment() {

    companion object {

        const val TAG = "RegisterSplitDialogFrag"
        const val EXTRA_STOCK_SYMBOL = "EXTRA_STOCK_SYMBOL"

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogRootView: View =
            LayoutInflater.from(context).inflate(R.layout.dialog_add_stock_split, view as ViewGroup?, false)
        val inputDate: EditText = dialogRootView.findViewById(R.id.editText_dialogAddStockSplit_dateInput)
        val inputLayoutDate: TextInputLayout =
            dialogRootView.findViewById(R.id.textInputLayout_dialogAddStockSplit_dateInput)
        inputDate.transformIntoDatePicker(requireContext(), DATE_FORMAT, Date(), inputLayoutDate)
        val reverseEnabledSwitch: SwitchMaterial =
            dialogRootView.findViewById(R.id.switchMaterial_dialogAddStockSplit_reverseEnabled)
        val inputSplitQuantity: AutoCompleteTextView =
            dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStockSplit_quantityInput)
        val inputLayoutSplitQuantity: TextInputLayout =
            dialogRootView.findViewById(R.id.textInputLayout_dialogAddStockSplit_quantityInput)
        val inputStockName: AutoCompleteTextView =
            dialogRootView.findViewById(R.id.autoCompleteTextView_dialogAddStockSplit_nameInput)
        inputStockName.setAdapter(getStockNameAutoCompleteAdapter())
        if (arguments?.containsKey(EXTRA_STOCK_SYMBOL) == true) {
            val stockTriple = StockPrice.SYMBOLS.find { triple ->
                triple.first == requireArguments().getString(EXTRA_STOCK_SYMBOL)
            }!!
            inputStockName.setText("${stockTriple.second} (${stockTriple.first})")
        }
        val inputLayoutStockName: TextInputLayout =
            dialogRootView.findViewById(R.id.textInputLayout_dialogAddStockSplit_nameInput)
        val confirmListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
        }
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogRootView)
            .setMessage(R.string.home_add_split_message)
            .setNegativeButton(getText(R.string.action_cancel)) { _, _ ->
                dismiss()
            }
            .setPositiveButton(getText(R.string.action_ok), confirmListener)
            .create()
        alertDialog.setOnShowListener {
            val button: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                try {
                    validateDate(inputDate, inputLayoutDate)
                    validateDouble(inputSplitQuantity, inputLayoutSplitQuantity)
                    validateStockName(inputStockName, inputLayoutStockName)
                    val stockSymbol = inputStockName.text.toString().substringAfterLast('(')
                        .substringBeforeLast(')')
                    val dateAsString = inputDate.text.toString()
                    val date = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateAsString)!!
                    val stockSplit = StockSplit(
                        reverseEnabledSwitch.isEnabled, date.time, stockSymbol,
                        inputSplitQuantity.text.toString().toInt()
                    )
                    homeViewModel.save(requireContext(), stockSplit)
                    Toast.makeText(requireContext(), getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                } catch (e: ValidatorException) {
                    // Shit happens ...
                }
            }
        }
        return alertDialog
    }

    private fun getStockNameAutoCompleteAdapter(): AutoCompleteAdapter {
        val searchArrayList =
            ArrayList(StockPrice.SYMBOLS.map { pair -> "${pair.second} (${pair.first})" }
                .toMutableList())
        return AutoCompleteAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line,
            android.R.id.text1, searchArrayList
        )
    }

    private fun validateStockName(input: AutoCompleteTextView, inputLayout: TextInputLayout) {
        when {
            input.text.toString().isEmpty() -> {
                inputLayout.error = getString(R.string.generic_error_empty)
                input.requestFocus()
                throw ValidatorException()
            }
            !StockPrice.SYMBOLS.map { pair -> "${pair.second} (${pair.first})" }
                .toMutableList().contains(input.text.toString()) -> {
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