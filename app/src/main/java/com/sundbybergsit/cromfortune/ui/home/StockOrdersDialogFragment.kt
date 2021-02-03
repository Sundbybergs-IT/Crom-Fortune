package com.sundbybergsit.cromfortune.ui.home

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
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl

class StockOrdersDialogFragment(private val stockSymbol: String) : DialogFragment() {

    private lateinit var listAdapter : StockOrderListAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogRootView: View = LayoutInflater.from(context)
                .inflate(R.layout.dialog_stock_orders, view as ViewGroup?, false)
        val recyclerView = dialogRootView.findViewById<RecyclerView>(R.id.recyclerView_dialogStockOrders)
        val context = requireContext()
        listAdapter = StockOrderListAdapter(context)
        recyclerView.adapter = listAdapter
        val stockOrderRepository = StockOrderRepositoryImpl(context)
        listAdapter.submitList(StockAdapterItemUtil.convertToAdapterItems(stockOrderRepository.list(stockSymbol)
                .sortedBy { stockOrder -> stockOrder.dateInMillis }))
        val stockName = StockPrice.SYMBOLS.find { pair -> pair.first == stockSymbol }!!.second
        return AlertDialog.Builder(context)
                .setView(dialogRootView)
                .setTitle(R.string.generic_title_stock_orders)
                .setMessage("$stockName (${stockSymbol})")
                .setPositiveButton(getText(R.string.action_close)) { _, _ ->
                }.create()
    }

}
