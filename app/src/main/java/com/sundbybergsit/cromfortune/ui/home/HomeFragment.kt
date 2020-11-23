package com.sundbybergsit.cromfortune.ui.home

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sundbybergsit.cromfortune.R
import java.util.*

class HomeFragment : Fragment() {

    companion object {

        const val TAG = "HomeFragment"

    }

    private lateinit var homeViewModel: HomeViewModel
    private val stockListAdapter = StockListAdapter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider.NewInstanceFactory().create(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val infoText: TextView = root.findViewById(R.id.textView_fragmentHome)
        val fab: FloatingActionButton = root.findViewById(R.id.floatingActionButton_fragmentHome)
        fab.setOnClickListener {
            val dialog = AddStockDialogFragment(homeViewModel)
            dialog.show(parentFragmentManager, TAG)
        }
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView_fragmentHome)
        stockListAdapter.setListener(homeViewModel)
        recyclerView.adapter = stockListAdapter
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
    }

    private fun setUpLiveDataListeners(textView: TextView, fab: FloatingActionButton) {
        homeViewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.ViewState.HasStocks -> {
                    textView.text = ""
                    fab.visibility = View.GONE
                    stockListAdapter.submitList(viewState.adapterItems)
                }
                is HomeViewModel.ViewState.HasNoStocks -> {
                    textView.text = getText(viewState.textResId)
                    fab.visibility = View.VISIBLE
                    stockListAdapter.submitList(Collections.emptyList())
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

    }

}
