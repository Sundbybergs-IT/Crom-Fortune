package com.sundbybergsit.cromfortune.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
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
        })
        dashboardViewModel.score.observe(viewLifecycleOwner, {
            textView_fragmentDashboard_score.text = it
        })
    }

}
