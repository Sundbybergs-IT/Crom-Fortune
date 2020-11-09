package com.sundbybergsit.cromfortune.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.StockPriceProducer
import com.sundbybergsit.cromfortune.ui.home.StockPriceRetriever
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.text_notifications
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel =
                ViewModelProvider.NewInstanceFactory().create(SettingsViewModel::class.java)
        setUpLiveDataListeners()
    }

    private fun setUpLiveDataListeners() {
        settingsViewModel.text.observe(viewLifecycleOwner, {
            editText_fragmentSettings_commissionFee.setOnClickListener {
                // TODO: Replace with logic to update commission fee
                Toast.makeText(requireContext(), getString(R.string.generic_error_not_supported), Toast.LENGTH_SHORT).show()
            }
        })
    }

}
