package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val viewModel: NotificationsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLiveDataListeners()
    }

    private fun setUpLiveDataListeners() {
        viewModel.refreshScore(requireContext())
        viewModel.score.observe(viewLifecycleOwner, {
            textView_fragmentNotifications_score.text = it
        })
        viewModel.text.observe(viewLifecycleOwner, {
            text_notifications.text = it
        })
    }

}
