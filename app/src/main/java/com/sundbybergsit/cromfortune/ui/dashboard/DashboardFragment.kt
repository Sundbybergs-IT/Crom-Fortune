package com.sundbybergsit.cromfortune.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.AdapterItemUtil

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var dashboardViewModel: DashboardViewModel

    private val stockListAdapter = StockListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel =
                ViewModelProvider.NewInstanceFactory().create(DashboardViewModel::class.java)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView_fragmentDashboard)
        recyclerView.adapter = stockListAdapter
        setupDataListeners()
    }

    private fun setupDataListeners() {
        dashboardViewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is DashboardViewModel.ViewState.OK -> {
                    stockListAdapter.submitList(AdapterItemUtil.convertToAdapterItems(dashboardViewModel.stocks(requireContext())))
                }
            }
        })
    }

}
