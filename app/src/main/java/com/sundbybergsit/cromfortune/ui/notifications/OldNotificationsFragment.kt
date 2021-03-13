package com.sundbybergsit.cromfortune.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sundbybergsit.cromfortune.R
import kotlinx.android.synthetic.main.fragment_notifications_archived.*
import java.util.*

class OldNotificationsFragment : Fragment(R.layout.fragment_notifications_archived) {

    companion object {

        const val TAG = "OldNotificationsFragment"

    }

    private val viewModel: NotificationsViewModel by activityViewModels()

    private val listAdapter = NotificationListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_fragmentNotificationsArchived.adapter = listAdapter
        setUpLiveDataListeners()
        viewModel.refreshOld(requireContext())
    }

    private fun setUpLiveDataListeners() {
        viewModel.notifications.observe(viewLifecycleOwner, { viewState ->
            when (viewState) {
                is NotificationsViewState.HasNotifications -> {
                    textView_fragmentNotificationsArchived.visibility = View.GONE
                    listAdapter.submitList(viewState.adapterItems)
                }
                is NotificationsViewState.HasNoOldNotifications -> {
                    listAdapter.submitList(Collections.emptyList())
                    textView_fragmentNotificationsArchived.visibility = View.VISIBLE
                    textView_fragmentNotificationsArchived.text = getString(R.string.notifications_empty)
                }
            }
        })
    }

}
