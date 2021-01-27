package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_notifications.*
import java.util.*

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val viewModel: NotificationsViewModel by viewModels()

    private val listAdapter = NotificationListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_fragmentNotifications.adapter = listAdapter
        setUpLiveDataListeners()
        viewModel.refresh(requireContext())
    }

    private fun setUpLiveDataListeners() {
        viewModel.score.observe(viewLifecycleOwner, {
            textView_fragmentNotifications_score.text = it
        })
        viewModel.notifications.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is NotificationsViewModel.ViewState.HasNotifications -> listAdapter.submitList(viewState.adapterItems)
                is NotificationsViewModel.ViewState.HasNoNotifications -> listAdapter.submitList(Collections.emptyList())
            }
        })
    }

}
