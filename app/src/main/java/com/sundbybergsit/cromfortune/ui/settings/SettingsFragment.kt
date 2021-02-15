package com.sundbybergsit.cromfortune.ui.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {

        const val TAG = "SettingsFragment"

    }

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpLiveDataListeners()
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.settings_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_showSupportedStocks -> {
                val dialog = SupportedStockDialogFragment()
                dialog.show(parentFragmentManager, TAG)
                true
            }
            R.id.action_todo -> {
                val dialog = ToDoDialogFragment()
                dialog.show(parentFragmentManager, TAG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpLiveDataListeners() {
        textInputLayout_fragmentSettings_commissionFee.setOnClickListener {
            // TODO: Replace with logic to update commission fee
            Toast.makeText(requireContext(), getString(R.string.generic_error_not_supported), Toast.LENGTH_SHORT).show()
        }
        textInputLayout_fragmentSettings_currency.setOnClickListener {
            // TODO: Replace with logic to update currency
            Toast.makeText(requireContext(), getString(R.string.generic_error_not_supported), Toast.LENGTH_SHORT).show()
        }
        viewModel.text.observe(viewLifecycleOwner, {
            textInputLayout_fragmentSettings_commissionFee.setOnClickListener {
            }
        })
        viewModel.todoText.observe(viewLifecycleOwner, {
            textView_fragmentSettings_todo.text = it
        })
    }

}
