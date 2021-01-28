package com.sundbybergsit.cromfortune.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.BuyStockCommand
import com.sundbybergsit.cromfortune.ui.home.SellStockCommand
import com.sundbybergsit.cromfortune.ui.home.StockPriceProducer
import com.sundbybergsit.cromfortune.ui.home.StockPriceRetriever
import com.sundbybergsit.cromfortune.ui.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.ui.notifications.NotificationUtil
import kotlinx.android.synthetic.main.fragment_dashboard.*

private const val STOCK_PRICE_REFRESH_INTERVAL = 60

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var dashboardViewModel: DashboardViewModel
    private val stockPriceRetriever: StockPriceRetriever = StockPriceRetriever(StockPriceProducer(),
            STOCK_PRICE_REFRESH_INTERVAL * 1000L, 0)

    private lateinit var infoText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel = ViewModelProvider.NewInstanceFactory().create(DashboardViewModel::class.java)
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
            Toast.makeText(requireContext(), "New real stock price: $stockPrice", Toast.LENGTH_SHORT).show()
            dashboardViewModel.refresh(requireContext(), stockPrice)
        })
        dashboardViewModel.score.observe(viewLifecycleOwner, {
            textView_fragmentDashboard_score.text = it
        })
        dashboardViewModel.recommendationViewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is DashboardViewModel.RecommendationViewState.NONE -> {
                    infoText.text = getString(R.string.recommendations_none)
                }
                is DashboardViewModel.RecommendationViewState.OK -> {
                    infoText.text = viewState.recommendation.toString()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), viewState.recommendation.toString(), Toast.LENGTH_LONG).show()
                        val notification = NotificationMessage(System.currentTimeMillis(),
                                viewState.recommendation.command.toString())
                        // TODO: Move repository logic
                        val notificationsRepository = NotificationsRepositoryImpl(requireContext())
                        notificationsRepository.add(notification)
                        val shortText: String =
                                when (viewState.recommendation.command) {
                                    is BuyStockCommand -> getString(R.string.action_stock_buy)
                                    is SellStockCommand -> getString(R.string.action_stock_sell)
                                    else -> ""
                                }
                        NotificationUtil.doPostRegularNotification(requireContext(),
                                getString(R.string.notification_recommendation_title),
                                shortText,
                                "${getString(R.string.notification_recommendation_body)} ${notification.message}")
                    }
                }
            }
        }
        )

    }

}
