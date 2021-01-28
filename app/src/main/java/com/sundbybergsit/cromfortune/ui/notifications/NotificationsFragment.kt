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
        viewModel.notifications.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is NotificationsViewModel.ViewState.HasNotifications -> {
                    textView_fragmentNotifications.visibility = View.GONE
                    listAdapter.submitList(viewState.adapterItems)
                }
                is NotificationsViewModel.ViewState.HasNoNotifications -> {
                    listAdapter.submitList(Collections.emptyList())
                    textView_fragmentNotifications.visibility = View.VISIBLE
                    textView_fragmentNotifications.text = getString(R.string.notifications_empty)
                }
            }
        })
    }

}
