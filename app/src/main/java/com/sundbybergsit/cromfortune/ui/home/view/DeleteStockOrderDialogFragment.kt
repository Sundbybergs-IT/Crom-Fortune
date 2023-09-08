package com.sundbybergsit.cromfortune.ui.home.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.DialogFragment
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderRepository
import com.sundbybergsit.cromfortune.domain.StockSplitRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockSplitRepositoryImpl
import com.sundbybergsit.cromfortune.ui.home.OpinionatedStockOrderWrapperListAdapter
import java.text.SimpleDateFormat
import java.util.*

class DeleteStockOrderDialogFragment(
    context: Context,
    private val splitRepository: StockSplitRepository = StockSplitRepositoryImpl(context),
    private val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context),
    val stockOrder: StockOrder,
    val adapter: OpinionatedStockOrderWrapperListAdapter,
) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            ConfigurationCompat.getLocales(requireContext().resources.configuration).get(0)
        )
        val context = requireContext()
        return AlertDialog.Builder(context)
            .setTitle(R.string.generic_dialog_title_are_you_sure)
            .setMessage(getString(R.string.home_delete_stock_order, formatter.format(Date(stockOrder.dateInMillis))))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                val listOfSplits = splitRepository.list(stockOrder.name)
                var isStockSplit = false
                for (split in listOfSplits) {
                    if (split.dateInMillis == stockOrder.dateInMillis) {
                        Log.i("DeleteStockOrderDialogFragment", "Assuming stock split... Removing.")
                        // Assume that this entry is a fake stock order and really is a split
                        splitRepository.remove(split)
                        isStockSplit = true
                    }
                }
                if (!isStockSplit) {
                    stockOrderRepository.remove(stockOrder)
                }
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }.create()
    }

}
