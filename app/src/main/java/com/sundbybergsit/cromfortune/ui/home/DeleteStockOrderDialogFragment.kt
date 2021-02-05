package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.DialogFragment
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import java.text.SimpleDateFormat
import java.util.*

class DeleteStockOrderDialogFragment(
        context: Context,
        private val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context),
        val stockOrder: StockOrder,
        val adapter: StockOrderListAdapter,
) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var formatter = SimpleDateFormat("yyyy-MM-dd", ConfigurationCompat.getLocales(requireContext().resources.configuration).get(0))
        val context = requireContext()
        return AlertDialog.Builder(context)
                .setTitle(R.string.generic_dialog_title_are_you_sure)
                .setMessage(getString(R.string.home_delete_stock_order, formatter.format(Date(stockOrder.dateInMillis))))
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    stockOrderRepository.remove(stockOrder)
                    adapter.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.action_cancel) { _, _ -> dismiss() }.create()
    }

}
