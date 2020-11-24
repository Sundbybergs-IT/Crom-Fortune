package com.sundbybergsit.cromfortune.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import com.sundbybergsit.cromfortune.ui.home.CromFortuneV1Decision
import com.sundbybergsit.cromfortune.ui.home.StockPriceProducer
import com.sundbybergsit.cromfortune.ui.home.StockPriceRetriever

private const val COMMISSION_FEE = 39.0
private const val STOCK_PRICE_REFRESH_INTERVAL = 60

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var dashboardViewModel: DashboardViewModel
    private val stockPriceRetriever: StockPriceRetriever = StockPriceRetriever(StockPriceProducer(),
            STOCK_PRICE_REFRESH_INTERVAL * 1000L, 0)

    private lateinit var infoText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel =
                ViewModelProvider.NewInstanceFactory().create(DashboardViewModel::class.java)
        infoText = view.findViewById(R.id.textView_fragmentDashboard)
        setupDataListeners()
    }

    override fun onResume() {
        super.onResume()
        stockPriceRetriever.start()
    }

    override fun onPause() {
        super.onPause()
        stockPriceRetriever.stop()
    }

    private fun setupDataListeners() {
        stockPriceRetriever.stockPrices.observe(viewLifecycleOwner, { stockPrice ->
            val recommendation = CromFortuneV1Decision(requireContext(),
                    requireContext().getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE))
                    .getRecommendation(stockPrice, COMMISSION_FEE)
            Toast.makeText(requireContext(), "New real stock price: $stockPrice", Toast.LENGTH_SHORT).show()
            if (recommendation != null) {
                infoText.text = recommendation.toString()
                Toast.makeText(requireContext(), recommendation.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

}
