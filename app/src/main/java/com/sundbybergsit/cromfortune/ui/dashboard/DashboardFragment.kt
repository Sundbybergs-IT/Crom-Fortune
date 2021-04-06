package com.sundbybergsit.cromfortune.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.databinding.FragmentDashboardBinding
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    companion object {

        const val TAG = "DashboardFragment"

    }

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataListeners()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolBar_fragmentDashboard)
                .setupWithNavController(navController, appBarConfiguration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            binding.textViewFragmentDashboardScore.text = it
        })
    }

}
