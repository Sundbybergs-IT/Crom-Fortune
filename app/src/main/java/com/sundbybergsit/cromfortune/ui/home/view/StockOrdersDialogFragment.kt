package com.sundbybergsit.cromfortune.ui.home.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockPrice
import com.sundbybergsit.cromfortune.ui.home.OpinionatedStockOrderWrapperListAdapter
import kotlinx.coroutines.runBlocking

class StockOrdersDialogFragment(
        private val stockSymbol: String, private val stocks: List<StockOrder>,
        private val readOnly: Boolean,
) : DialogFragment() {

    private lateinit var listAdapter: OpinionatedStockOrderWrapperListAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogRootView: View = LayoutInflater.from(context)
                .inflate(R.layout.dialog_stock_orders, view as ViewGroup?, false)
        val recyclerView = dialogRootView.findViewById<RecyclerView>(R.id.recyclerView_dialogStockOrders)
        val context = requireContext()
        listAdapter = OpinionatedStockOrderWrapperListAdapter(context = context, fragmentManager = parentFragmentManager,
                readOnly = readOnly)
        recyclerView.adapter = listAdapter
        runBlocking {
            listAdapter.submitList(OpinionatedStockOrderWrapperAdapterItemUtil.convertToAdapterItems(
                    CromFortuneV1RecommendationAlgorithm(context), stocks))
        }
        val stockName = StockPrice.SYMBOLS.find { pair -> pair.first == stockSymbol }!!.second
        return AlertDialog.Builder(context)
                .setView(dialogRootView)
                .setTitle(R.string.generic_title_stock_orders)
                .setMessage("$stockName (${stockSymbol})")
                .setPositiveButton(getText(R.string.action_close)) { _, _ ->
                }.create()
    }

}
