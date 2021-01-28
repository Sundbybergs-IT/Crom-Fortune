package com.sundbybergsit.cromfortune.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sundbybergsit.cromfortune.R
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    companion object {

        const val TAG = "HomeFragment"

    }

    private val viewModel: HomeViewModel by viewModels()

    private val stockListAdapter = StockListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val infoText: TextView = view.findViewById(R.id.textView_fragmentHome)
        val infoImage: ImageView = view.findViewById(R.id.imageView_fragmentHome)
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton_fragmentHome)
        fab.setOnClickListener {
            val dialog = RegisterBuyStockDialogFragment(viewModel)
            dialog.show(parentFragmentManager, TAG)
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView_fragmentHome)
        stockListAdapter.setListener(viewModel)
        recyclerView.adapter = stockListAdapter
        setUpLiveDataListeners(infoText, infoImage)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh(requireContext())
    }

    private fun setUpLiveDataListeners(textView: TextView, infoImage: ImageView) {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.ViewState.HasStocks -> {
                    textView.text = ""
                    infoImage.visibility = View.GONE
                    stockListAdapter.submitList(viewState.adapterItems)
                }
                is HomeViewModel.ViewState.HasNoStocks -> {
                    textView.text = getText(viewState.textResId)
                    infoImage.visibility = View.VISIBLE
                    stockListAdapter.submitList(Collections.emptyList())
                }
            }
        })
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

}
