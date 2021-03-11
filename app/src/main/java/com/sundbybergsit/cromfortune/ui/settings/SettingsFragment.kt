package com.sundbybergsit.cromfortune.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {

        const val TAG = "SettingsFragment"

    }

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpLiveDataListeners()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar_fragmentSettings.setupWithNavController(navController, appBarConfiguration)
        toolBar_fragmentSettings.inflateMenu(R.menu.settings_actions)
        toolBar_fragmentSettings.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_stockRetrievalIntervals -> {
                    val dialog = TimeIntervalStockRetrievalDialogFragment()
                    dialog.show(parentFragmentManager, TAG)
                    true
                }
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
        super.onViewCreated(view, savedInstanceState)
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
