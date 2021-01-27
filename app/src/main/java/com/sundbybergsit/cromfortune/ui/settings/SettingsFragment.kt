package com.sundbybergsit.cromfortune.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLiveDataListeners()
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
