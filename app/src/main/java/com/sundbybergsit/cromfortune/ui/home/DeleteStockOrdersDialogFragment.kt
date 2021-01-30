package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.sundbybergsit.cromfortune.R

class DeleteStockOrdersDialogFragment(private val homeViewModel: HomeViewModel, val stockName: String) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return AlertDialog.Builder(context)
                .setTitle(R.string.generic_dialog_title_are_you_sure)
                .setMessage(getString(R.string.home_delete_all_stock_orders, stockName))
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    homeViewModel.confirmRemove(requireContext(), stockName)
                }
                .setNegativeButton(R.string.action_cancel) { _, _ -> dismiss() }.create()
    }

}
