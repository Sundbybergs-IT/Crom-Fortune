package com.sundbybergsit.cromfortune.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {

        const val TAG = "SettingsFragment"

    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpLiveDataListeners()
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolBarFragmentSettings.setupWithNavController(navController, appBarConfiguration)
        binding.toolBarFragmentSettings.inflateMenu(R.menu.settings_actions)
        binding.toolBarFragmentSettings.setOnMenuItemClickListener { item ->
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
        binding.textInputLayoutFragmentSettingsCommissionFee.setOnClickListener {
            // TODO: Replace with logic to update commission fee
            Toast.makeText(requireContext(), getString(R.string.generic_error_not_supported), Toast.LENGTH_SHORT).show()
        }
        binding.textInputLayoutFragmentSettingsCurrency.setOnClickListener {
            // TODO: Replace with logic to update currency
            Toast.makeText(requireContext(), getString(R.string.generic_error_not_supported), Toast.LENGTH_SHORT).show()
        }
        viewModel.text.observe(viewLifecycleOwner, {
            binding.textInputLayoutFragmentSettingsCommissionFee.setOnClickListener {
            }
        })
        viewModel.todoText.observe(viewLifecycleOwner, {
            binding.textViewFragmentSettingsTodo.text = it
        })
    }

}
