package com.sundbybergsit.cromfortune.ui.home.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.home.DeleteStockOrdersDialogFragment
import com.sundbybergsit.cromfortune.ui.home.HomeViewModel
import com.sundbybergsit.cromfortune.ui.home.trade.RegisterBuyStockDialogFragment
import kotlinx.android.synthetic.main.fragment_home_stocks.*
import java.util.*

class HomePersonalStocksFragment : Fragment(R.layout.fragment_home_stocks), StockClickListener {

    companion object {

        const val TAG = "HomePersonalStocksFragm"

    }

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var stockOrderAggregateListAdapter: StockOrderAggregateListAdapter
    private var currencyRatesLoaded = false
    private var stockPricesLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stockOrderAggregateListAdapter = StockOrderAggregateListAdapter(this)
        floatingActionButton_fragmentHomeStocks.setOnClickListener {
            val dialog = RegisterBuyStockDialogFragment(viewModel)
            dialog.show(parentFragmentManager, TAG)
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView_fragmentHomeStocks)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        stockOrderAggregateListAdapter.setListener(viewModel)
        recyclerView.adapter = stockOrderAggregateListAdapter
        setUpLiveDataListeners(textView_fragmentHomeStocks, imageView_fragmentHomeStocks,
                floatingActionButton_fragmentHomeStocks)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setUpLiveDataListeners(textView: TextView, infoImage: ImageView, fab: FloatingActionButton) {
        setUpCurrencyRateListener()
        setUpStockPriceListener()
        setUpUiViewStateListener(textView, fab, infoImage)
        setUpDialogViewStateListener()
    }

    private fun setUpDialogViewStateListener() {
        viewModel.dialogViewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.DialogViewState.ShowDeleteDialog -> {
                    val dialog = DeleteStockOrdersDialogFragment(homeViewModel = viewModel,
                            stockName = viewState.stockName)
                    dialog.show(parentFragmentManager, TAG)
                }
            }
        })
    }

    private fun setUpUiViewStateListener(textView: TextView, fab: FloatingActionButton, infoImage: ImageView) {
        viewModel.personalStocksViewState.observe(viewLifecycleOwner, { viewState ->
            Log.i(TAG, "New stock list view state.")
            when (viewState) {
                is HomeViewModel.ViewState.Loading -> {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_fragmentHomeStocks).visibility = View.VISIBLE
                    textView.text = ""
                    fab.visibility = View.GONE
                    infoImage.visibility = View.GONE
                }
                is HomeViewModel.ViewState.HasStocks -> {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_fragmentHomeStocks).visibility = View.GONE
                    textView.text = ""
                    infoImage.visibility = View.GONE
                    fab.visibility = View.GONE
                    stockOrderAggregateListAdapter.submitList(viewState.adapterItems)
                }
                is HomeViewModel.ViewState.HasNoStocks -> {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_fragmentHomeStocks).visibility = View.GONE
                    textView.text = getText(viewState.textResId)
                    infoImage.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                    stockOrderAggregateListAdapter.submitList(Collections.emptyList())
                }
            }
        })
    }

    private fun setUpStockPriceListener() {
        StockPriceRepository.stockPrices.observe(viewLifecycleOwner, { viewState: StockPriceRepository.ViewState ->
            Log.i(TAG, "New stock price view state.")
            when (viewState) {
                is StockPriceRepository.ViewState.VALUES -> {
                    stockPricesLoaded = true
                    if (currencyRatesLoaded) {
                        refreshEverything()
                    }
                }
            }
        })
    }

    private fun setUpCurrencyRateListener() {
        CurrencyRateRepository.currencyRates.observe(viewLifecycleOwner, { viewState ->
            Log.i(TAG, "New currency rate view state.")
            when (viewState) {
                is CurrencyRateRepository.ViewState.VALUES -> {
                    currencyRatesLoaded = true
                    if (stockPricesLoaded) {
                        refreshEverything()
                    }
                }
            }
        })
    }

    private fun refreshEverything() {
        (requireView().findViewById(R.id.floatingActionButton_fragmentHomeStocks) as FloatingActionButton).isEnabled = true
        viewModel.refresh(requireContext())
        if (stockOrderAggregateListAdapter.itemCount > 0) {
            stockOrderAggregateListAdapter.onBindViewHolder(
                    StockOrderAggregateListAdapter.StockOrderAggregateHeaderViewHolder(stockPriceListener = stockOrderAggregateListAdapter,
                            itemView = LayoutInflater.from(context).inflate(R.layout.listrow_stock_header, requireView()
                                    .findViewById(R.id.recyclerView_fragmentHomeStocks), false),
                            context = requireContext()), 0)
            stockOrderAggregateListAdapter.notifyDataSetChanged()
        }
    }

    override fun onClick(stockName: String) {
        val dialog = StockOrdersDialogFragment(stockName)
        dialog.show(parentFragmentManager, TAG)
    }

}
