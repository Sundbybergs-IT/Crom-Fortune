package com.sundbybergsit.cromfortune.ui.home

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sundbybergsit.cromfortune.R
import java.util.*

class HomeFragment : Fragment() {

    companion object {

        const val TAG = "HomeFragment"

    }

    private val viewModel: HomeViewModel by viewModels()

    private val stockListAdapter = StockListAdapter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val infoText: TextView = root.findViewById(R.id.textView_fragmentHome)
        val infoImage: ImageView = root.findViewById(R.id.imageView_fragmentHome)
        val fab: FloatingActionButton = root.findViewById(R.id.floatingActionButton_fragmentHome)
        fab.setOnClickListener {
            val dialog = RegisterBuyStockDialogFragment(viewModel)
            dialog.show(parentFragmentManager, TAG)
        }
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView_fragmentHome)
        stockListAdapter.setListener(viewModel)
        recyclerView.adapter = stockListAdapter
        setUpLiveDataListeners(infoText, infoImage, fab)
        setHasOptionsMenu(true)
        return root
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

    private fun setUpLiveDataListeners(textView: TextView, infoImage: ImageView, fab: FloatingActionButton) {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.ViewState.HasStocks -> {
                    textView.text = ""
                    infoImage.visibility = View.GONE
                    fab.visibility = View.GONE
                    stockListAdapter.submitList(viewState.adapterItems)
                }
                is HomeViewModel.ViewState.HasNoStocks -> {
                    textView.text = getText(viewState.textResId)
                    infoImage.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
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
