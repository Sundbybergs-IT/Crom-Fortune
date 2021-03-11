package com.sundbybergsit.cromfortune.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    companion object {

        const val TAG = "DashboardFragment"

    }

    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataListeners()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolBar_fragmentDashboard)
                .setupWithNavController(navController, appBarConfiguration)
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
