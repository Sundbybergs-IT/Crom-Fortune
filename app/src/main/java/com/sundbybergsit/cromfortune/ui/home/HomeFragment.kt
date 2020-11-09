package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StocksPreferences

private const val STOCK_PRICE_REFRESH_INTERVAL = 60
private const val COMMISSION_FEE = 39.0

class HomeFragment : Fragment() {

    companion object {

        const val TAG = "HomeFragment"

    }

    private lateinit var homeViewModel: HomeViewModel
    private val stockPriceRetriever: StockPriceRetriever = StockPriceRetriever(StockPriceProducer(),
            STOCK_PRICE_REFRESH_INTERVAL * 1000L, 0)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider.NewInstanceFactory().create(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val infoText: TextView = root.findViewById(R.id.textView_fragmentHome)
        val fab: FloatingActionButton = root.findViewById(R.id.floatingActionButton_fragmentHome);
        fab.setOnClickListener {
            val dialog = AddStockDialogFragment(homeViewModel)
            dialog.show(parentFragmentManager, TAG)
        }
        setUpLiveDataListeners(infoText, fab)
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_addStock -> {
                val dialog = AddStockDialogFragment(homeViewModel)
                dialog.show(parentFragmentManager, TAG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refresh(requireContext())
        stockPriceRetriever.start()
    }

    override fun onPause() {
        super.onPause()
        stockPriceRetriever.stop()
    }

    private fun setUpLiveDataListeners(textView: TextView, fab: FloatingActionButton) {
        homeViewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.ViewState.HasStocks -> {
                    textView.text = ""
                    fab.visibility = View.GONE
                }
                is HomeViewModel.ViewState.HasNoStocks -> {
                    textView.text = getText(viewState.textResId)
                    fab.visibility = View.VISIBLE
                }
            }
        })
        homeViewModel.addStockState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.AddStockState.Error -> {
                    Toast.makeText(requireContext(), getText(viewState.errorResId), Toast.LENGTH_SHORT).show()
                }
                is HomeViewModel.AddStockState.Saved -> {
                    Toast.makeText(requireContext(), getText(R.string.generic_saved), Toast.LENGTH_SHORT).show()
                }
            }
        })
        stockPriceRetriever.stockPrices.observe(viewLifecycleOwner, { stockPrice ->
            val recommendation = CromFortuneV1Decision(requireContext(),
                    requireContext().getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE))
                    .getRecommendation(stockPrice, COMMISSION_FEE)
            Toast.makeText(requireContext(), "New real stock price: $stockPrice", Toast.LENGTH_SHORT).show()
            if (recommendation != null) {
                textView.text = recommendation.toString()
                Toast.makeText(requireContext(), recommendation.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

}
