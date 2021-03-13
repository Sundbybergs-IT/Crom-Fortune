package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_notifications_current.*
import java.util.*

class NewNotificationsFragment : Fragment(R.layout.fragment_notifications_current) {

    companion object {

        const val TAG = "NewNotificationsFragment"

    }

    private val viewModel: NotificationsViewModel by activityViewModels()

    private val listAdapter = NotificationListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_fragmentNotificationsCurrent.adapter = listAdapter
        setUpLiveDataListeners()
        viewModel.refreshNew(requireContext())
    }

    private fun setUpLiveDataListeners() {
        viewModel.newNotifications.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is NotificationsViewState.HasNotifications -> {
                    textView_fragmentNotificationsCurrent.visibility = View.GONE
                    listAdapter.submitList(viewState.adapterItems)
                }
                is NotificationsViewState.HasNoNotifications -> {
                    listAdapter.submitList(Collections.emptyList())
                    textView_fragmentNotificationsCurrent.visibility = View.VISIBLE
                    textView_fragmentNotificationsCurrent.text = getString(R.string.notifications_empty)
                }
            }
        })
    }

}
