package com.sundbybergsit.cromfortune.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.home.BuyStockCommand
import com.sundbybergsit.cromfortune.ui.home.SellStockCommand
import com.sundbybergsit.cromfortune.ui.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.ui.notifications.NotificationUtil
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    private lateinit var infoText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        infoText = view.findViewById(R.id.textView_fragmentDashboard)
        setupDataListeners()
    }

    private fun setupDataListeners() {
        StockPriceRepository.stockPrices.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is StockPriceRepository.ViewState.VALUES -> {
                    dashboardViewModel.refresh(requireContext(), viewState.instant, viewState.stockPrices)
                }
            }
        }
        )
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
