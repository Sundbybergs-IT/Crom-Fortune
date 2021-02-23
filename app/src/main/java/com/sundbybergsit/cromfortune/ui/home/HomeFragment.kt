package com.sundbybergsit.cromfortune.ui.home

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home), StockClickListener {

    companion object {

        const val TAG = "HomeFragment"

    }

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var stockOrderAggregateListAdapter: StockOrderAggregateListAdapter
    private var currencyRatesLoaded = false
    private var stockPricesLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stockOrderAggregateListAdapter = StockOrderAggregateListAdapter(this)
        val infoText: TextView = view.findViewById(R.id.textView_fragmentHome)
        val infoImage: ImageView = view.findViewById(R.id.imageView_fragmentHome)
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton_fragmentHome)
        fab.setOnClickListener {
            val dialog = RegisterBuyStockDialogFragment(viewModel)
            dialog.show(parentFragmentManager, TAG)
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView_fragmentHome)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        stockOrderAggregateListAdapter.setListener(viewModel)
//        recyclerViewAdapterDataObserver = RefreshAdapterHeaderRecyclerViewAdapterDataObserver(requireContext(),
//                stockOrderAggregateListAdapter, recyclerView)
//        stockOrderAggregateListAdapter.registerAdapterDataObserver(recyclerViewAdapterDataObserver)
        recyclerView.adapter = stockOrderAggregateListAdapter
        setUpLiveDataListeners(infoText, infoImage, fab)
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_buyStock -> {
                val dialog = RegisterBuyStockDialogFragment(viewModel)
                dialog.show(parentFragmentManager, TAG)
                true
            }
            R.id.action_sellStock -> {
                val dialog = RegisterSellStockDialogFragment(viewModel)
                dialog.show(parentFragmentManager, TAG)
                true
            }
            R.id.action_refresh -> {
                viewModel.refreshData(requireContext())
                Toast.makeText(context, R.string.home_information_data_refreshed, Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpLiveDataListeners(textView: TextView, infoImage: ImageView, fab: FloatingActionButton) {
        setUpCurrencyRateListener()
        setUpStockPriceListener()
        setUpUiViewStateListener(textView, fab, infoImage)
        setUpStockTransactionStateListener()
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

    private fun setUpStockTransactionStateListener() {
        viewModel.stockTransactionState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.StockTransactionState.Error -> {
                    Toast.makeText(requireContext(), getText(viewState.errorResId), Toast.LENGTH_SHORT).show()
                }
                is HomeViewModel.StockTransactionState.Saved -> {
                    Toast.makeText(requireContext(), getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setUpUiViewStateListener(textView: TextView, fab: FloatingActionButton, infoImage: ImageView) {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.ViewState.Loading -> {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_fragmentHome).visibility = View.VISIBLE
                    textView.text = ""
                    fab.visibility = View.GONE
                    infoImage.visibility = View.GONE
                }
                is HomeViewModel.ViewState.HasStocks -> {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_fragmentHome).visibility = View.GONE
                    textView.text = ""
                    infoImage.visibility = View.GONE
                    fab.visibility = View.GONE
                    stockOrderAggregateListAdapter.submitList(viewState.adapterItems)
                }
                is HomeViewModel.ViewState.HasNoStocks -> {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_fragmentHome).visibility = View.GONE
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
        (requireView().findViewById(R.id.floatingActionButton_fragmentHome) as FloatingActionButton).isEnabled = true
        viewModel.refresh(requireContext())
        if (stockOrderAggregateListAdapter.itemCount > 0) {
            stockOrderAggregateListAdapter.onBindViewHolder(
                    StockOrderAggregateListAdapter.StockOrderAggregateHeaderViewHolder(stockPriceListener = stockOrderAggregateListAdapter,
                            itemView = LayoutInflater.from(context).inflate(R.layout.listrow_stock_header, requireView()
                                    .findViewById(R.id.recyclerView_fragmentHome), false),
                            context = requireContext()), 0)
            stockOrderAggregateListAdapter.notifyDataSetChanged()
        }
    }

    override fun onClick(stockName: String) {
        val dialog = StockOrdersDialogFragment(viewModel, stockName)
        dialog.show(parentFragmentManager, TAG)
    }

}
