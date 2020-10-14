package com.sundbybergsit.cromfortune.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sundbybergsit.cromfortune.R

class HomeFragment : Fragment() {

    companion object {

        const val TAG = "HomeFragment"

    }
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider.NewInstanceFactory().create(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        val fab: FloatingActionButton = root.findViewById(R.id.floatingActionButton);

        fab.setOnClickListener {
            val dialog = AddStockDialogFragment(homeViewModel)
            dialog.show(parentFragmentManager, TAG)
        }
        setUpLiveDataListeners(textView, fab)
        return root
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refresh(requireContext())
    }

    private fun setUpLiveDataListeners(textView: TextView, fab: FloatingActionButton) {
        homeViewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is HomeViewModel.ViewState.HasStocks -> {
                    textView.text = getText(viewState.textResId)
                    fab.visibility = View.GONE
                }
                is HomeViewModel.ViewState.HasNoStocks -> {
                    textView.text = getText(viewState.textResId)
                    fab.visibility = View.VISIBLE
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
